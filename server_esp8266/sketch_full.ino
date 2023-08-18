#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <EncButton.h>
#include <ESP8266WebServer.h>
#include <ESP8266WebServerSecure.h>

#include "EEPROMHelper.h"
#include "LedStripHelper.h"
#include "WebServerSecureHelper.h"

// Fix a conflict between two libraries that has "nullptr" defined
#ifdef nullptr
#undef nullptr
#endif

// #define DEBUG_ENABLED
#ifdef DEBUG_ENABLED
#define PRINT_DEBUG(x) Serial.println(x)
#else
#define PRINT_DEBUG(x)
#endif

#pragma region TouchButton
#define PIN_BTN 13  //WEMOS D7
EncButton<EB_CALLBACK, PIN_BTN> touchButton;
#pragma endregion
#pragma region WebServer
ESP8266WebServer server(80);
BearSSL::ESP8266WebServerSecure authServer(443);
BearSSL::ServerSessions serverCache(LED_SERVER_CACHE_SIZE);

#define CONTENTTYPE_JSON "application/json"
#define SERVER_200_RESPONSE "{\"message\": \"Success\"}"
#define SERVER_500_RESPONSE "{\"message\": \"Internal Server Error\""
#define SERVER_400_RESPONSE "{\"message\": \"Invalid input from user\"}"
#define SSID_MAX_LENGTH 32
#define PASS_MAX_LENGTH 32

#define FIRST_LOAD_FLAG_ADDR 64
#define STATE_MODE_ADDR 65
#define STATE_BRIGHTNESS_ADDR 70
#define STATE_COLOR_ADDR 71

byte AUTH_SERVER_STARTED = 0;
byte SERVER_STARTED = 0;
#pragma endregion

void setup() {
#ifdef DEBUG_ENABLED
  Serial.begin(115200);
#endif

  touchButtonInit();
  ledHelper.init();
  eepromHelper.init();
  serverLedLampInit();

  byte isFirstLoad = EEPROM.read(FIRST_LOAD_FLAG_ADDR);
  PRINT_DEBUG("Is First Load:");
  PRINT_DEBUG(isFirstLoad);

  if (isFirstLoad == 1)
    setupLedStateFromMemory();
  else
    setupLedStateInMemory();
}

void loop() {
  touchButton.tick();
  serverHandleClient();
  ledHelper.setMode(ledHelper.getState().mode);
}

void serverHandleClient() {
  if (AUTH_SERVER_STARTED == 1) {
    authServer.handleClient();
  } else if (SERVER_STARTED == 1) {
    server.handleClient();
  } else
    PRINT_DEBUG("No server handling clients");
}

#pragma region WebServer Initialization
void serverLedLampInit() {
  String eepromSsid;
  String eepromPassword;
  eepromHelper.readToString(0, SSID_MAX_LENGTH, eepromSsid);
  eepromHelper.readToString(SSID_MAX_LENGTH, SSID_MAX_LENGTH + PASS_MAX_LENGTH, eepromPassword);

  PRINT_DEBUG("Trying to access existing WiFi access point...");
  WiFi.begin(eepromSsid.c_str(), eepromPassword.c_str());

  wl_status_t connectionResult = wl_status_t(WiFi.waitForConnectResult());

  if (connectionResult != WL_CONNECTED) {
    PRINT_DEBUG("WiFi Access Point not found. Trying to enable HotSpot mode...");
    WiFi.disconnect();
    initAuthServer();
  } else {
    initServer();
  }
}

void initAuthServer() {
  bool softApConnected = WiFi.softAP(tempSsid, tempPass, 1, 2, 1);
  if (softApConnected) {
    PRINT_DEBUG(WiFi.softAPIP());
    PRINT_DEBUG("Soft AP mode started.");
    authServer.on("/setup", HTTP_POST, handleSetup);
    authServer.on("/reset", HTTP_POST, handleHardReset);

    configTime(3 * 3600, 0, "pool.ntp.org", "time.nist.gov");
    authServer.getServer().setRSACert(new BearSSL::X509List(server_cert), new BearSSL::PrivateKey(server_private_key));
    authServer.getServer().setCache(&serverCache);
    AUTH_SERVER_STARTED = 1;
    authServer.begin();
  } else {
    PRINT_DEBUG("Failed to connect to AP and start AP itself.");
    ledHelper.setMode(ERROR);
  }
}

void initServer() {
  AUTH_SERVER_STARTED = 0;
  PRINT_DEBUG(WiFi.localIP());

  PRINT_DEBUG("STA mode started.");
  server.on("/modes", HTTP_GET, handleGetModes);
  server.on("/state", HTTP_POST, handleChangeState);
  server.on("/state", HTTP_GET, handleGetState);
  server.on("/reset", HTTP_POST, handleHardReset);
  SERVER_STARTED = 1;
  server.begin();
}

#pragma endregion

#pragma region WebServer Handlers
void handleSetup() {
  String argSsid = authServer.arg("ssid");
  String argPass = authServer.arg("pass");
  int ssidLength = argSsid.length();
  int passLength = argPass.length();

  PRINT_DEBUG("SSID from request: ");
  PRINT_DEBUG(argSsid);
  PRINT_DEBUG("Password from request: ");
  PRINT_DEBUG(argPass);

  if (ssidLength > SSID_MAX_LENGTH || passLength > PASS_MAX_LENGTH)
    authServer.send(400, CONTENTTYPE_JSON, SERVER_400_RESPONSE);

  eepromHelper.clear(SSID_MAX_LENGTH + PASS_MAX_LENGTH);

  eepromHelper.writeString(argSsid, 0);
  eepromHelper.writeString(argPass, SSID_MAX_LENGTH);

  bool writeToMemoryResult = EEPROM.commit();
  if (writeToMemoryResult) {
    PRINT_DEBUG("SSID and password saved to memory.");
    authServer.send(200, CONTENTTYPE_JSON, SERVER_200_RESPONSE);
  } else {
    authServer.send(500, CONTENTTYPE_JSON, SERVER_500_RESPONSE);
  }
  ESP.restart();
}

void handleHardReset() {
  eepromHelper.clear(STATE_COLOR_ADDR + 4);
  EEPROM.end();
  PRINT_DEBUG("Resetting ESP");
  server.send(200, CONTENTTYPE_JSON, SERVER_200_RESPONSE);

  ESP.restart();
}

void handleGetModes() {
  PRINT_DEBUG("HANDLE GET MODES:");
  String JSON;
  StaticJsonDocument<500> doc;
  JsonArray modes = doc.createNestedArray("modes");

  for (byte i = 0; i < sizeof(mapping) / sizeof(mapping[0]); i++) {
    modes.add(mapping[i].text);
  }
  serializeJson(doc, JSON);
  PRINT_DEBUG(JSON);
  server.send(200, CONTENTTYPE_JSON, JSON);
}

void handleGetState() {
  PRINT_DEBUG("HANDLE GET STATE:");
  String JSON;
  StaticJsonDocument<100> doc;

  setupLedStateFromMemory();

  doc["mode"] = getEnumText(ledHelper.getState().mode);
  doc["color"] = ledHelper.getState().color;
  doc["brightness"] = ledHelper.getState().brightness;

  serializeJson(doc, JSON);

  server.send(200, CONTENTTYPE_JSON, JSON);
}
void handleChangeState() {
  String mode = server.arg("mode");
  String brightness = server.arg("brightness");
  String color = server.arg("color");

  PRINT_DEBUG("ARGUMENTS FROM REQUEST:");
  PRINT_DEBUG("MODE:");
  PRINT_DEBUG(mode);
  PRINT_DEBUG("BRIGHT:");
  PRINT_DEBUG(brightness);
  PRINT_DEBUG("COlOR:");
  PRINT_DEBUG(color);

  if (mode.length() != 0)
    changeMode(parseToEnum(mode.c_str()));
  if (brightness.length() != 0)
    changeBrightness(brightness.toInt());
  if (color.length() != 0)
    changeColor(color.toInt());

  server.send(200, CONTENTTYPE_JSON, SERVER_200_RESPONSE);
}

void changeMode(Mode mode) {
  PRINT_DEBUG("CHANGE MODE");
  PRINT_DEBUG(mode);
  saveMode(mode);
}

void changeBrightness(byte brightness) {
  PRINT_DEBUG("CHANGE BRIGHT");
  PRINT_DEBUG(brightness);
  ledHelper.setBrightness(brightness);
  EEPROM.write(STATE_BRIGHTNESS_ADDR, ledHelper.getState().brightness);
  EEPROM.commit();
}

void changeColor(int color) {
  PRINT_DEBUG("CHANGE COLOR");
  PRINT_DEBUG(color);

  ledHelper.setColor(color);
  EEPROM.put(STATE_COLOR_ADDR, ledHelper.getState().color);
  EEPROM.commit();
}
#pragma endregion

#pragma region TouchButton
void touchButtonInit() {
  touchButton.setButtonLevel(HIGH);
  touchButton.attach(CLICK_HANDLER, handleClick);
}

void handleClick() {
  PRINT_DEBUG("click");
  ledHelper.switchToNextMode();
  saveMode(ledHelper.getState().mode);
}
#pragma endregion

void setupLedStateInMemory() {
  EEPROM.put(STATE_MODE_ADDR, (Mode)0);
  EEPROM.write(STATE_BRIGHTNESS_ADDR, BRIGHTNESS_INIT);
  EEPROM.put(STATE_COLOR_ADDR, DEFAULT_COLOR);

  EEPROM.write(FIRST_LOAD_FLAG_ADDR, 1);
  EEPROM.commit();
}

void setupLedStateFromMemory() {
  LedState state;

  EEPROM.get(STATE_MODE_ADDR, state.mode);
  EEPROM.get(STATE_BRIGHTNESS_ADDR, state.brightness);
  EEPROM.get(STATE_COLOR_ADDR, state.color);

  ledHelper.setState(&state);
}

void saveMode(Mode m){
   ledHelper.setMode(m);
  if (m != OFF && m != ON && m != ERROR) {
    PRINT_DEBUG("SAVING MODE");
    EEPROM.put(STATE_MODE_ADDR, m);
    EEPROM.commit();
  }
}

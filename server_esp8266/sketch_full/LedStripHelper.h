#pragma once
#include <FastLED.h>
#include <TimerMs.h>

#define MODE_NUM 6
#define BRIGHTNESS_INIT 20
#define MAX_BRIGHTNESS 100
#define DEFAULT_COLOR 16777215  //white
#define NUM_LEDS 60
#define PIN_LEDS 2  //WEMOS D4

enum Mode {
  RAINBOW,
  CONFETTI,
  FIRE,
  COLOR,
  ON,
  OFF,
  ERROR
};

const static struct {
  Mode value;
  const char* text;
} mapping[] = {
  { RAINBOW, "rainbow" },
  { CONFETTI, "confetti" },
  { FIRE, "fire" },
  { COLOR, "color" },
  { ON, "spec_on" },
  { OFF, "spec_off" },
  { ERROR, "spec_error" }
};

struct LedState {
  Mode mode;        // 4 byte
  byte brightness;  //1 byte
  int color;        //4 byte
};

Mode parseToEnum(const char* str) {
  for (int i = 0; i < sizeof(mapping) / sizeof(mapping[0]); i++) {
    if (!strcmp(str, mapping[i].text))
      return mapping[i].value;
  }
  return ERROR;
}

String getEnumText(Mode mode) {
  for (int i = 0; i < sizeof(mapping) / sizeof(mapping[0]); i++) {
    if ((Mode)mode ==  mapping[i].value)
      return mapping[i].text;
  }
  return "error";
}

class LedStripHelper {
public:
  LedStripHelper(){};
  void init();
  void setState(LedState* state);
  void setMode(Mode mode);
  void switchToNextMode();
  void setColor(int color);
  void setBrightness(byte brightness);
  LedState getState();

private:
  TimerMs _tmr;
  byte _counter;
  byte _modeCounter;
  CRGB _leds[NUM_LEDS];
  LedState _currentState;
  void rainbow();
  void colorLight(int color);
  void confetti();
  void fire();
  void off();
  void errorIndication();
};

LedStripHelper ledHelper = LedStripHelper();

void LedStripHelper::init() {
  _tmr = TimerMs(40, 1, 1);
  _tmr.setPeriodMode();
  _counter = 0;
  _modeCounter = 0;

  FastLED.addLeds<WS2812, PIN_LEDS, GRB>(_leds, NUM_LEDS);
}

void LedStripHelper::setState(LedState* state) {
  _currentState.brightness = state->brightness;
  _currentState.mode = state->mode;
  _currentState.color = state->color;
}

void LedStripHelper::setMode(Mode mode) {
  _currentState.mode = mode;
  switch (_currentState.mode) {
    case ON:
      colorLight(DEFAULT_COLOR);
      break;
    case RAINBOW:
      rainbow();
      break;
    case COLOR:
      colorLight(_currentState.color);
    case CONFETTI:
      confetti();
      break;
    case FIRE:
      fire();
      break;
    case OFF:
      off();
      break;
    case ERROR:
      errorIndication();
      break;
  }
}

void LedStripHelper::switchToNextMode() {
  if (_modeCounter < MODE_NUM - 2) {
    _modeCounter++;
  } else {
    _modeCounter = 0;
  }
  setMode((Mode)_modeCounter);
}

void LedStripHelper::setColor(int color) {
  _currentState.color = color;
  colorLight(_currentState.color);
}

void LedStripHelper::setBrightness(byte brightness) {
  if (brightness > MAX_BRIGHTNESS)
    _currentState.brightness = BRIGHTNESS_INIT;
  else
    _currentState.brightness = brightness;

  FastLED.setBrightness(255 * _currentState.brightness / 100);
}

LedState LedStripHelper::getState() {
  return _currentState;
}


void LedStripHelper::rainbow() {
  if (_tmr.tick()) {
    FastLED.setBrightness(255 * _currentState.brightness / 100);
    for (int i = 0; i < NUM_LEDS; i++) {
      _leds[i].setHue(_counter + i * 255 / NUM_LEDS);
    }
    _counter++;
    FastLED.show();
  }
}

void LedStripHelper::colorLight(int color) {
  FastLED.setBrightness(255 * _currentState.brightness / 100);
  for (int i = 0; i < NUM_LEDS; i++) {
    _leds[i] = color;
  }
  FastLED.show();
}

void LedStripHelper::confetti() {
  fadeToBlackBy(_leds, NUM_LEDS, 2);
  byte i = random16(NUM_LEDS);
  _leds[i] += CHSV(_counter + random8(64), 200, 255);
  _counter++;
  FastLED.show();
}

void LedStripHelper::fire() {
  fadeToBlackBy(_leds, NUM_LEDS, 2);
  int i = beatsin16(13, 0, NUM_LEDS - 1);
  _leds[i] += CHSV(_counter, 255, 192);
  _counter++;
  FastLED.show();
}

void LedStripHelper::off() {
  FastLED.clear(true);
  _modeCounter = 0;
  _currentState.brightness = BRIGHTNESS_INIT;
  FastLED.setBrightness(255 * _currentState.brightness / 100);
  FastLED.show();
}

void LedStripHelper::errorIndication() {
  FastLED.setBrightness(BRIGHTNESS_INIT);
  for (int i = 0; i < NUM_LEDS; i++) {
    _leds[i] = CRGB::Red;
  }
  FastLED.show();
}
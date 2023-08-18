#define LED_SERVER_CACHE_SIZE 5
#define TEMP_AP_SSID "ENTER SSDI FOR AP MODE"
#define TEMP_AP_PASS "ENTER PASSWORD FOR AP MODE"

const char* tempSsid = TEMP_AP_SSID;
const char* tempPass = TEMP_AP_PASS;

#pragma region Certificate
const char server_private_key[] PROGMEM = R"EOF(
-----BEGIN PRIVATE KEY-----
!!!! YOUR KEY HERE !!!!
-----END PRIVATE KEY-----
)EOF";

const char server_cert[] PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
!!!! YOUR CERTIFICATE HERE !!!!
-----END CERTIFICATE-----  
)EOF";

#pragma endregion

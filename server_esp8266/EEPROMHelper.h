#pragma once
#include <EEPROM.h>

class EEPROM_Helper {
public:
  EEPROM_Helper(){};
  void init();
  void readToString(int startOffset, int length, String& s);
  void writeString(String& str, int startOffset);
  void clear(int length);
};

void EEPROM_Helper::init() {
  EEPROM.begin(512);
}

void EEPROM_Helper::readToString(int startOffset, int length, String& s) {
  for (byte i = startOffset; i < length; i++)
    s += char(EEPROM.read(i));
}

void EEPROM_Helper::writeString(String& str, int startOffset) {
  int length = str.length();
  for (byte i = 0; i < length; i++) {
    EEPROM.write(i + startOffset, str[i]);
  }
}

void EEPROM_Helper::clear(int length) {
  for (byte i = 0; i < length; i++) {
    EEPROM.write(i, 0);
  }
}

EEPROM_Helper eepromHelper = EEPROM_Helper();
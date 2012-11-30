#define START_COLOR_CHAR '^'
#define END_CHAR '$'
#define COLOR_SIZE 8
//--
#define PIN_RED 9
#define PIN_GREEN 10
#define PIN_BLUE 11

char serialMessage[COLOR_SIZE];
unsigned int readChar;
unsigned int count;
boolean readingSerial;
unsigned int r;
unsigned int g;
unsigned int b;

/*
 *
 */
void setup() {
  Serial.begin(115200);
  readingSerial = false;
}

/*
 *
 */
void loop() {
  if (Serial.available() > 0 && !readingSerial) {
    char first = Serial.read();

    if (first == START_COLOR_CHAR) {
      serialReadColor();
    } 
  } 
}

/*
 *
 */
void serialReadColor() {
  readingSerial = true;
  count = 0;

  while(true) {
    if (Serial.available() > 0) {
      readChar = Serial.read();
      if (readChar == END_CHAR || count == COLOR_SIZE) {
        break;
      } 
      else {
        serialMessage[count++] = readChar;
      }
    }
  }

  serialMessage[count] = '\0';

  unsigned long color = atol(serialMessage);

  r = color >> 16 & 0xFF;
  g = color >>  8 & 0xFF;
  b = color >>  0 & 0xFF;

  analogWrite(PIN_RED, r);
  analogWrite(PIN_GREEN, g);
  analogWrite(PIN_BLUE, b);

  readingSerial = false;
}

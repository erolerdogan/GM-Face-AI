int data = 5;
int LED1 = 22;
int LED2 = 24;
int LED3 = 23;
int LED4 = 26;
int LED5 = 25;
int LED6 = 10;

void setup() {
  // put your setup code here, to run once:
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
  pinMode(LED5, OUTPUT);
  pinMode(LED6, OUTPUT);
  Serial.begin(9600); // Seri portumuza baund veriyoruz.
}

void loop() {
  if (Serial.available() > 0) { /* Serial1 erişilebilir durumdaysa true değer alır ve aşağıdaki kodları çalıştırır.*/
   
    Serial.println("Donguye girdi");
    int data = Serial.read();
    Serial.println(data);
    // 1 saniye bekliyoruz.
    if (data == 49) {
      digitalWrite(LED1, LOW);
      digitalWrite(LED2, HIGH);

      Serial.println("YAKK");
    }
    if (data == 48) {
    
    digitalWrite(LED1, HIGH);
    digitalWrite(LED2, LOW);
    
      Serial.println("SONDURRRR");
    }
  }
}

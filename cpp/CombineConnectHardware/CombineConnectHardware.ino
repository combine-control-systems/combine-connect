#include "BluetoothSerial.h"
#define WIRESERIAL Serial1
// ----Object Declarations-----------------------------------------------------
BluetoothSerial SerialBT;
bool isConnected = false;

// ----ISR functions ----------------------------------------------------------
void callback(esp_spp_cb_event_t event, esp_spp_cb_param_t *param){
  if(event == ESP_SPP_SRV_OPEN_EVT){
    while(WIRESERIAL.available()>=1){
      WIRESERIAL.read();
    }
    while(SerialBT.available()>=1){
      SerialBT.read();  
    }
    isConnected = true;
  }
 
  if(event == ESP_SPP_CLOSE_EVT ){
    isConnected = false;
    while(WIRESERIAL.available()>=1){
      WIRESERIAL.read();  
    }
    while(SerialBT.available()>=1){
      SerialBT.read();  
    }
    
    SerialBT.flush();
    delay(100);
  }
}
// the setup function runs once when you press reset or power the board
void setup() {
  //Runs by default on core 1. All ISR started here will run at core 1
  isConnected = false;
  // initialize serial communication at 115200 bits per second:
  Serial.begin(115200);
  WIRESERIAL.begin(115200);

  SerialBT.register_callback(callback);

  while(!SerialBT.begin("CombineConnect")){
    delay(100);
  }

  xTaskCreatePinnedToCore(
    taskIncomingBluetoothCommunication
    ,  "taskIncomingBluetoothCommunication"   //Arbitrary name
    ,  2048  // This stack size can be checked & adjusted by reading the Stack Highwater
    ,  NULL
    ,  3  // Priority, with 3 (configMAX_PRIORITIES - 1) being the highest, and 0 being the lowest.
    ,  NULL 
    ,  0); // core

  xTaskCreatePinnedToCore(
    taskOutgoingBluetoothCommunication
    ,  "taskOutgoingBluetoothCommunication"   //Arbitrary name
    ,  2048  // This stack size can be checked & adjusted by reading the Stack Highwater
    ,  NULL
    ,  3  // Priority, with 3 (configMAX_PRIORITIES - 1) being the highest, and 0 being the lowest.
    ,  NULL 
    ,  1); // core

  //while(!isConnected){
  //  delay(10);
  //}
}

void loop()
{
   
}

void taskIncomingBluetoothCommunication(void *pvParameters)  //Bluetooth Communication Task
{
  //SETUP

  /*
  Serial.print("BT task is running at core ");
  Serial.println(xPortGetCoreID());*/
  const TickType_t xDelay = 1 / portTICK_PERIOD_MS;
  Serial.println("starting task");
  
  while(true) 
  {  
    if(isConnected){
      Serial.println("connected!");
      //Incoming Bluetooth Communication: 
      while(SerialBT.available()>=1){
        char c = SerialBT.read();
        WIRESERIAL.write(c);
        //digitalWrite(LED_BUILTIN, HIGH);
      }
    }
    vTaskDelay(xDelay);
  }
  Serial.println("deleting task");
  vTaskDelete(NULL);
}

void taskOutgoingBluetoothCommunication(void *pvParameters)  //Bluetooth Communication Task
{
  //SETUP

  /*
  Serial.print("BT task is running at core ");
  Serial.println(xPortGetCoreID());*/
  const TickType_t xDelay = 1 / portTICK_PERIOD_MS;
  Serial.println("starting task");
  
  while(true)
  {  
    if(isConnected){
      while(WIRESERIAL.available()>=1){
        char c = WIRESERIAL.read();
        //if(transmitOK){
        SerialBT.write(c);  
        //}
      }  
    }
    vTaskDelay(xDelay);
  }
  Serial.println("deleting task");
  vTaskDelete(NULL);
}
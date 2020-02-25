#include "FolkRace.h"
#include "CombineConnect.h"
#include "IRsensor.h"
#include "IIRFirstOrder.h"
#include "MotorDriver.h"
#include "Steering.h"
#include "PID.h"

IRsensor leftSensor(LEFTSENSOR);
IRsensor rightSensor(RIGHTSENSOR);
IIR_firstOrder leftFilter(0.6);
IIR_firstOrder rightFilter(0.6);
MotorDriver motorDriver(MOTORFORWARD,MOTORREVERSE);
Steering steering(SERVOPIN);
PID PIDController;

CombineConnect combineConnect(&blueTooth,&wireSerial);

bool delayUntil(float TsMicroseconds);
void updateSensors();

void setup() {
  // put your setup code here, to run once:
  TsMicroseconds = Ts*1000000;
  // IF ON BOARD HC05 USE 9600:
  blueTooth.begin(9600);
  // IF EXTERNAL DEVICE (ESP32) USE 115200
  //blueTooth.begin(115200);
  wireSerial.begin(115200);
  delay(1000);

  
  motorDriver.init();
  steering.init();

  combineConnect.addFloatPlotData(&leftDistance,"Left Sensor");
  combineConnect.addFloatPlotData(&rightDistance,"Right Sensor");
  combineConnect.addFloatPlotData(&debug,"debug variable");
  combineConnect.addFloatPlotData(&steeringAngleDeg,"Steering angle");

  combineConnect.addMainState(MainState::RUN,"Run",true);
  combineConnect.addMainState(MainState::STOP,"Stop",true);
  combineConnect.addMainState(MainState::FORWARD,"Forward");
  combineConnect.addMainState(MainState::BACKWARD,"Backwards");

  combineConnect.addMode('l',"Enable LED");
  combineConnect.addMode('p',"Enable Plot",true);

  lastSampleTime = micros();
  Serial.println("setup done!");

}

void loop() {
  if(combineConnect.update()){ 
    currentMainState = combineConnect.getCurrentMainState();
    Kp = combineConnect.getCalibrationData("P");
    Ki = combineConnect.getCalibrationData("I"); 
    Kd = combineConnect.getCalibrationData("D");
    vel = combineConnect.getCalibrationData("v");
  }
  updateSensors();
  if(delayUntil(TsMicroseconds)){
    switch(currentMainState){
      case MainState::RUN:
        
        PIDController.setKp(Kp);
        PIDController.setKi(Ki);
        PIDController.setKd(Kd);

        deltaPos = leftDistance-rightDistance;
        steeringAngleDeg = -PIDController.update(0,deltaPos);
        steering.setSteeringAngleDeg(steeringAngleDeg);

        /*
        float vel_dec = 0.25*abs(leftDistance-rightDistance);
        if(vel - vel_dec <= 0){
          vel_dec = vel;
        }
    	  else if(vel - vel_dec < 60){
      		vel_dec = vel-60;
    	  }

        motorDriver.setPWM(vel-vel_dec);
        */
        motorDriver.setPWM(vel);

        break;
      case MainState::STOP:
        PIDController.setKp(Kp);
        PIDController.setKi(Ki);
        PIDController.setKd(Kd);

        deltaPos = leftDistance-rightDistance;
        steeringAngleDeg = -PIDController.update(0,deltaPos);
        steering.setSteeringAngleDeg(steeringAngleDeg);
        motorDriver.setPWM(0);
        break;
      case MainState::FORWARD:
        Serial.println("FORWARD!");
        break;
      case MainState::BACKWARD:
        Serial.println("BACKWARDS!");
        break;
      case ';':
        float angle = combineConnect.getAngle();
        steering.setSteeringAngleDeg(-angle);
        float power = 130;
        motorDriver.setPWM(combineConnect.getPower()*power);
        break;
      default:
        //Idle
        PIDController.setKp(Kp);
        PIDController.setKi(Ki);
        PIDController.setKd(Kd);

        deltaPos = leftDistance-rightDistance;
        steeringAngleDeg = -PIDController.update(0,deltaPos);
        steering.setSteeringAngleDeg(steeringAngleDeg);
        motorDriver.setPWM(0);
        break;    
    }
  
  }
  if(combineConnect.getCurrentMode('p') == 1){
    combineConnect.transmitPlotData();
  }
  if(combineConnect.getCurrentMode('l') == 1){
    combineConnect.transmitDebugMessage("Enable LED");
  }
  String dbmsg = combineConnect.getDebugMessage();
  if(dbmsg.equals("get system time")){
    combineConnect.transmitDebugMessage("Time since reboot = "+String(millis()) + "ms.");
  }
  if(dbmsg.equals("Highly customizable debug message")){
    combineConnect.transmitDebugMessage("Received a highly customized debug message! Doing a highly customized thing in response!");
  }
  if(dbmsg.equals("led enabled?")){
    combineConnect.transmitDebugMessage("false");
  }  

}


bool delayUntil(float TsMicroseconds){
  unsigned long currentTime = micros();
  if(currentTime - lastSampleTime > TsMicroseconds){
    debug = ((float(currentTime)-lastSampleTime)/1000.0);
    lastSampleTime = currentTime;
    return true;
  }
  return false;
}

void updateSensors(){
  leftSensorReading = 1000*leftSensor.readDistance();
  rightSensorReading = 1000*rightSensor.readDistance();
  leftDistance = leftFilter.update(leftSensorReading);
  rightDistance = rightFilter.update(rightSensorReading);
}

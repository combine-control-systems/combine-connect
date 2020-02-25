// 
// 
// 

#include "MotorDriver.h"

MotorDriver::MotorDriver(uint8_t __PINfwd, uint8_t __PINrev) : PINfwd(__PINfwd), PINrev(__PINrev)
{
	init(); 
}

bool MotorDriver::init()
{

	pinMode(PINfwd, OUTPUT);
	pinMode(PINrev, OUTPUT);

	digitalWrite(PINfwd, LOW);
	digitalWrite(PINrev, LOW);

	//TODO: implement some check here if needed. 
	return false; 
}

void MotorDriver::setPWM(int16_t PWM){

	//Clamp
	if(PWM > maxPWM){
		PWM = maxPWM; 
	}
	else if (PWM < -int(maxPWM))
	{
		PWM = -maxPWM; 
	}
	
	//Set forward, reverse torque/current reference. 
	if (PWM > 0){
		analogWrite(PINfwd, PWM); 
		analogWrite(PINrev, LOW); 
	}
	else if (PWM < 0) {
		analogWrite(PINfwd, LOW);
		analogWrite(PINrev, -PWM);
	}
	else {
		analogWrite(PINfwd, LOW); 
		analogWrite(PINrev, LOW); 
	}
}



// 
// 
// 

#include "Steering.h"



Steering::Steering(int servoPin)
{
	servo.attach(servoPin); 
}

void Steering::init()
{
	setSteeringAngleDeg(0);
	delay(100); 
}

/*Set the steering angle of the wheels with respect to the chassis. When this angle is around 0 the 
servo, with the super class PWMservo sees around 90 degrees. TODO: Should probably contain a non linear map*/
void Steering::setSteeringAngleDeg(float angleDeg)
{
	int angleToWrite = angles::neutral; 

	if (angleDeg !=0)
	{
		angleToWrite = angles::neutral - angleDeg; //Minussign for the current coordinate system assumption to be correct. 
	}
	
	//Clamp/saturate angle 
	if (angleToWrite > angles::maxRight)
	{
		angleToWrite = angles::maxRight; 
	}
	else if (angleToWrite < angles::maxLeft)
	{
		angleToWrite = angles::maxLeft;
	}

	//Write desired angle to servo 
	setPWM(angleToWrite);  
}

void Steering::setPWM(int val){
    servo.write(val);
}

// Steering.h

#ifndef _STEERING_h
#define _STEERING_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <Servo.h>


class Steering
{
public: 
	Steering(int __servoPin);

	/** Inits pins to inputs, outputs etc.  */
	void init(); 

	/** Set PWM ratio in the range \f[PWM \in (-256,256)\f] A negative number is interpreted as motor reversal.*/
	void setSteeringAngleDeg(float angleDeg);
	void setPWM(int val); 

private: 
	
	enum angles {
		neutral = 90,
		maxRight = neutral +30,
		maxLeft = neutral -30
	};

	Servo servo;
};

/** @}*/

#endif


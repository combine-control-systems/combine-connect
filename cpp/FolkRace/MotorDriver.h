// motorDriver.h

#ifndef _MOTORDRIVER_h
#define _MOTORDRIVER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

/** \defgroup Motordriver Motor driver
 *  @{
 */

/**@brief Driver for a simple H bridge with only two inputs, that was used in this project. At the time of writing this library we used the MX1508 motor driver. 

http://www.techmonkeybusiness.com/using-the-mx1508-brushed-dc-motor-driver-with-an-arduino.html */
class MotorDriver
{
public: 
	MotorDriver(uint8_t __PINfwd, uint8_t __PINrev);

	/** Inits pins to inputs, outputs etc.  */
	bool init(); 

	/** Set PWM ratio in the range \f[PWM \in (-256,256)\f] A negative number is interpreted as motor reversal.*/
	void setPWM(int16_t PWM); 

private: 
	
	const uint16_t maxPWM = 256; 
	const uint8_t PINfwd, PINrev;
};

/** @}*/

#endif


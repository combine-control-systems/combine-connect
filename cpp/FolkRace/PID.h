// PID.h

#ifndef _PID_h
#define _PID_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif



class PID
{
public: 
	PID(float PInit = 0,float IInit = 0,float DInit = 0,float saturation = 0);

	float update(float setPoint,float processValue);
    void setKp(float Kp);
    void setKi(float Ki);
    void setKd(float Kd);


private: 
	
    float Kp,Ki,Kd,saturation;
    bool useSaturation = false;
    float lastMicros;
    float lastFilteredError;

    float antiWindupConst = 1;
    float Pout,Iout,Dout,du;
	
};

/** @}*/

#endif


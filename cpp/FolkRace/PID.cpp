#include "PID.h"

PID::PID(float PInit/*= 0*/,float IInit/*= 0*/,float DInit/*= 0*/,float saturation/*= 0*/){
    Kp = PInit;
    Ki = IInit;
    Kd = DInit;

    if(saturation!=0){
        this->saturation = saturation;
        useSaturation = true;
    }else{
        useSaturation = false;
    }

    lastMicros = micros();
}

float PID::update(float setPoint,float processValue){
    float out;

    float error = setPoint - processValue;
    float currTime = float(micros());
    float deltaT = (currTime - lastMicros)*0.000001F;
    lastMicros = currTime;

    //PROPORTIONAL
	Pout = Kp*error;

    //DERIVATIVE
    Dout = Kd*(error - lastFilteredError) / deltaT; 
	lastFilteredError = error; 

    //INTEGRATING WITH anti windup by BACK CALCULATION METHOD
  	Iout = Iout + Ki*deltaT*error+ antiWindupConst*du;

    out = Pout + Iout + Dout; 

    du = out - (Pout + Iout + Dout);

    return out;
}

void PID::setKp(float Kp){
    this -> Kp = Kp;
}

void PID::setKi(float Ki){
    this -> Ki = Ki;
}

void PID::setKd(float Kd){
    this -> Kd = Kd;
}

// 
// 
// 

#include "IRsensor.h"

IRsensor::IRsensor(uint8_t __PIN) : PIN(__PIN)
{
	pinMode(PIN,INPUT);
}

/** Read IR sensor value from ADC and convert result to meters usings a polynomial fit previously acquired. */
float IRsensor::readDistance()
{
	ADCraw = analogRead(PIN); 

	if (ADCraw > maxADCval) ADCraw = maxADCval; 

	//TODO: Add appopriate filtering, possibly removal of wild values and so on 
	ADCfilt = float(ADCraw); 

	float ADC_sqr = ADCfilt * ADCfilt; 
	float ADC_cub = ADC_sqr * ADCfilt; 

	dist = p1 * ADC_cub + p2 * ADC_sqr + p3 * ADCfilt + p4; 

	if(dist > 600){
		dist = 600;
	}

	return dist; 
}

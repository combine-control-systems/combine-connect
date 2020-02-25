// IRsensor.h

#ifndef _IRSENSOR_h
#define _IRSENSOR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

/** \defgroup Sensors Sensors
Functionalities related to reading the sensors. 
 *  @{
 */
/**
@brief Read the GP2Y0A60SZLF Sharp sensor on a 3.3 system. Conversion of output to meters is done using a previous 3rd order polynomial fit according to 

\f[ d = p_1 u^3 + p_2 u^2 +p3 u^1  + p4u^0  \f]

where u is the ADC value. The higher the value, the shorter the distance. 

TODO: The ADC value should be filtered appropriately, a suggestion is using a median filter. 
*/

class IRsensor
{
public:
	/**Connect sensor to a pin. It must be checked manually forehand that the sensor has an ADC converter. */
	IRsensor(uint8_t PIN_);
	

	float readDistance(); 
	uint16_t ADCraw; 
	

private:
	uint8_t PIN; 
	
	float ADCfilt; 
	float dist; 

	/** Third order polynomial fit from matlab distance = p1*x^3 +p2*x^2 + p3*x + p4 */
	float p4 = 1.169088730869346;
	float p3 = -0.005874173911976;
	float p2 = 0.000011107571857; 
	float p1 = -0.000000007136922; 

	const uint16_t maxADCval = 600; /** This is the highest ADC val when we saw the sensor make any kind of sense */

	//const float minDistance = 0.05; /** This is the lowest value where the sensor make any kind of sense */


};

/** @}*/


#endif

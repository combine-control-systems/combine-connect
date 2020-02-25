// IIR_firstOrder.h

#ifndef _IIR_FIRSTORDER_h
#define _IIR_FIRSTORDER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

/** \defgroup Stateestimation State estimation and filtering
A modules where filters and state estimation modules are collected. 
 *  @{
 */

/**
@brief A simple first order discrete IIR filter updating output according to 
\f[y_k = au_{k-1}+by_{k-1}  \f]

It is tuned according to a single parameter x according to 

\f[a = 1-x, \quad b = x \f]

The filter dynamics depends on the sampling frequency and should be designed according to proper methods,
such as frequency domain specification. However, for quick and dirty applications x can be set experimentally. Set it closer to unity for 
"more filtering". It can be shown that for a forward Eulers discretization of a cont. first order system having the time constant Tc the paramter x is 

\f[ x = T_s/T_c = T_s \omega_b \f]

where Ts is the sampling time of the discrete system . So, if the sampling time is constand and known, the discrete filter can be specified using a time constant or bandwidth 

\f[\omega_b = \frac{1}{T_c} \f]

By Martin P�r-Love Palm 2019

*/
class IIR_firstOrder
{
public:
	/** 
	 @param x Is a single parameter defining the filter dynamics. It has to be between 0-1 for stability of the filter. The closer to unity (x) the slower it gets, and "filters" more
	 .  If an attempt is made to set it outside these 
	 boundaries the filter is effectively bypasssed */
	IIR_firstOrder(float x);
	/** 
	@param update filter output using input u 
	*/
	float update(float u);

private:
	float a; 
	float b; 
	float y=0, lastY=0; 

};


/** @}*/

#endif


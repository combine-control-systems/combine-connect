// 
// 
// 

#include "IIRFirstOrder.h"

IIR_firstOrder::IIR_firstOrder(float x)
{
	if(x>=0 && x <1)
	{
		a = (1 - x); 
		b = x; 
	}
	else
	{
		a = 1; 
		b = 0; 
	}
}

float IIR_firstOrder::update(float u)
{
	y = a * u + b * lastY; 
	lastY = y; 
	return y; 
}

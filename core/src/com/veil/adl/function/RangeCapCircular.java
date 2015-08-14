package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class RangeCapCircular implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		float num = ((Number)param[0]).floatValue();
		float min = ((Number)param[1]).floatValue();
		float max = ((Number)param[2]).floatValue();
		float range = max-min+1;
		while(num < min){
			num += range;
		}
		while(num > max){
			num -= range;
		}
		return num;
	}

}

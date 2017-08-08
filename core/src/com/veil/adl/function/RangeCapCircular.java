package com.veil.adl.function;

import java.math.BigDecimal;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class RangeCapCircular implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		float num = ((Number)param[0]).floatValue();
		float min = ((Number)param[1]).floatValue();
		float max = ((Number)param[2]).floatValue();
		if(max < min){
			float tmp = max;
			max = min;
			min = tmp;
		}
		
		BigDecimal bigMin = new BigDecimal(min);
		BigDecimal bigMax = new BigDecimal(max);
		BigDecimal range = new BigDecimal(max-min+1);
		BigDecimal bigNum = new BigDecimal(num);
		
		if(bigNum.compareTo(bigMin) < 0){
			bigNum = bigMax.subtract(bigNum.divideAndRemainder(range)[1]);
		}else if(bigNum.compareTo(bigMax) > 0){
			bigNum = bigMin.add(bigNum.divideAndRemainder(range)[1]);
		}
		/*
		while(bigNum.compareTo(bigMin) < 0){
			bigNum = bigNum.add(range);
		}
		while(bigNum.compareTo(bigMax) > 0){
			bigNum = bigNum.subtract(range);
		}
		*/
		return bigNum.floatValue();
	}

}

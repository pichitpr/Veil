package com.veil.adl.function;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class DecimalSet implements IFunction {
	
	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Float start = ((Number)param[0]).floatValue();
		Float end = ((Number)param[1]).floatValue();
		Float step = ((Number)param[2]).floatValue();
		
		//Adjust value
		if(end < start){
			float tmp = end;
			end = start;
			start = tmp;
		}
		step = Math.abs(step);
		if(step < 1) step = 1f;
		
		//Scale value to prevent performance hit
		if((end - start)/step > 100){
			step = (end-start)/100f;
		}
		
		List<Float> numSet = new ArrayList<Float>();
		BigDecimal bigStart = new BigDecimal(start);
		BigDecimal bigEnd = new BigDecimal(end);
		while(bigStart.compareTo(bigEnd) <= 0){
			numSet.add(bigStart.floatValue());
			bigStart = bigStart.add(new BigDecimal(step));
		}
		return numSet;
	}

}

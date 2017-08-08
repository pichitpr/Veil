package com.veil.adl.function;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class DecimalSetSymmetry implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Float center = ((Number)param[0]).floatValue();
		Float range = ((Number)param[1]).floatValue();
		
		//Adjust value
		range = Math.abs(range);
		if(range < 1) range = 1f;
		Float step = ((Number)param[2]).floatValue();
		step = Math.abs(step);
		if(step < 1) step = 1f;
		
		//Scale value to prevent performance hit
		if(range*2/step > 100){
			step = range/50f;
		}
		
		List<Float> numSet = new ArrayList<Float>();
		//Very large step = permanent overflow = endless loop here
		BigDecimal value = new BigDecimal(center-range);
		BigDecimal bound = new BigDecimal(center+range);
		while(value.compareTo(bound) <= 0){
			numSet.add(value.floatValue());
			value = value.add(new BigDecimal(step));
		}
		return numSet;
	}

	
}

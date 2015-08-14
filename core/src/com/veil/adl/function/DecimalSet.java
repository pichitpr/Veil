package com.veil.adl.function;

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
		List<Float> numSet = new ArrayList<Float>();
		while(start <= end){
			numSet.add(start);
			start += step;
		}
		return numSet;
	}

}

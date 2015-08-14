package com.veil.adl.function;

import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class DecimalSetSymmetry implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Float center = ((Number)param[0]).floatValue();
		Float range = ((Number)param[1]).floatValue();
		Float step = ((Number)param[2]).floatValue();
		List<Float> numSet = new ArrayList<Float>();
		float value = center-range;
		while(value <= range){
			numSet.add(value);
			value += step;
		}
		return numSet;
	}

	
}

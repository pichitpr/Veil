package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

public class Abs implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		return Math.abs(((Number)param[0]).floatValue());
	}

}

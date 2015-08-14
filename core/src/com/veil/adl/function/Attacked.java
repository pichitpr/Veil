package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class Attacked implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		return ((ItpData)itp.getAdditionalData()).thisAgent.flag.attacked;
	}

}

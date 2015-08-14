package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class TravelDistance implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		return ((ItpData)itp.getAdditionalData()).distance;
	}

}

package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

public class Debug implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		System.out.println(param[0]);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

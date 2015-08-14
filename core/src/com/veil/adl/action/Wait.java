package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

public class Wait implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		return (Boolean)param[0];
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

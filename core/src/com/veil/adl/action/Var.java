package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class Var implements IAction {

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		int var = param[0].toString().hashCode();
		((ItpData)itp.getAdditionalData()).mainItp.getVarTable().put(var, 0);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

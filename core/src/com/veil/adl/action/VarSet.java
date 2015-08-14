package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class VarSet implements IAction {

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		int var = param[0].toString().hashCode();
		int value = ((Number)param[1]).intValue();
		((ItpData)itp.getAdditionalData()).mainItp.getVarTable().put(var, value);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

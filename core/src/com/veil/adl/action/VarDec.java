package com.veil.adl.action;

import adl_2daa.AgentModelInterpreter;
import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class VarDec implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		int var = param[0].toString().hashCode();
		AgentModelInterpreter aItp = ((ItpData)itp.getAdditionalData()).mainItp;
		if(aItp.getVarTable().containsKey(var)){
			aItp.getVarTable().put(var, aItp.getVarTable().get(var)-1);
		}
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

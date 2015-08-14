package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class Goto implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		String identifier = param[0].toString();
		((ItpData)itp.getAdditionalData()).mainItp.pendingNextState(identifier);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

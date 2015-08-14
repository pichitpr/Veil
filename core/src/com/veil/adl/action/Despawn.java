package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;

public class Despawn implements IAction {

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		dyn.despawn();
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

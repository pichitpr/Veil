package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.game.element.DynamicEntity;

public class Notify implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity target = (DynamicEntity)param[0];
		int message = ((Number)param[1]).intValue();
		target.flag.notify(message);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		// TODO Auto-generated method stub
		return false;
	}

}

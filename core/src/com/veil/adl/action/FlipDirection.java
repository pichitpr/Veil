package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class FlipDirection implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		Direction axis = Direction.parse(param[0]);
		axis.rotate(90);
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		float deg = dyn.direction.deltaDegree(axis);
		dyn.direction.rotate(-deg*2);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

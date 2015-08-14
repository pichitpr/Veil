package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class Rel implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		Position pos = Position.parse(param[0]);
		return new Position(dyn.getWorldCenteredPosition().x+pos.getX(),
				dyn.getWorldCenteredPosition().y+pos.getY());
	}

}

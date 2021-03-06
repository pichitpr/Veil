package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class Rel2Direction implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		Direction dir = Direction.parse(param[0]);
		Direction result = new Direction(dyn.direction);
		result.rotate(dir.getDegree());
		return result;
	}

}

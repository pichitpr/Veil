package com.veil.adl.function;

import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class TurnTo implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		@SuppressWarnings("unchecked")
		List<Direction> dSet = (List<Direction>)param[0];
		Position pos = Position.parse(param[1]);
		Direction dir = new Direction(pos.getX()-dyn.getWorldCenteredPosition().x,
				pos.getY()-dyn.getWorldCenteredPosition().y);
		return Direction.getNearestFromSet(dSet, dir);
	}

}

package com.veil.adl.function;

import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class TurnToPlayer implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		DynamicEntity dyn = itpData.thisAgent;
		LevelContainer level = itpData.level;
		@SuppressWarnings("unchecked")
		List<Direction> dSet = (List<Direction>)param[0];
		Direction dir = new Direction(
				level.getPlayer().getWorldCenteredPosition().x-dyn.getWorldCenteredPosition().x,
				level.getPlayer().getWorldCenteredPosition().y-dyn.getWorldCenteredPosition().y);
		return Direction.getNearestFromSet(dSet, dir);
	}

}

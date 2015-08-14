package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.level.LevelContainer;

public class RelPlayer implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		Position pos = Position.parse(param[0]);
		return new Position(level.getPlayer().getWorldCenteredPosition().x+pos.getX(),
				level.getPlayer().getWorldCenteredPosition().y+pos.getY());
	}

}

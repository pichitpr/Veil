package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class DistanceToPlayer implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		DynamicEntity dyn = itpData.thisAgent;
		LevelContainer level = itpData.level;
		String axis = param[0].toString();
		if(axis.equalsIgnoreCase("Y")){
			return dyn.getWorldCenteredPosition().y - level.getPlayer().getWorldCenteredPosition().y;
		}else if(axis.equalsIgnoreCase("X")){
			return dyn.getWorldCenteredPosition().x - level.getPlayer().getWorldCenteredPosition().x;
		}else
			return null;
	}

}
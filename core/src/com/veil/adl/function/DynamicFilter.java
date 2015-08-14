package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class DynamicFilter implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		String identifier = param[0].toString();
		DynamicEntity dyn = itpData.thisAgent;
		LevelContainer level = itpData.level;
		if(identifier.equalsIgnoreCase("this")){
			return dyn;
		}else if(identifier.equalsIgnoreCase("player")){
			return level.getPlayer();
		}
		return null;
	}

}

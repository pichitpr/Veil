package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class Peak implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		DynamicEntity dyn = itpData.thisAgent;
		LevelContainer level = itpData.level;
		boolean result = false;
		if(level.getGravity() != 0){
			result = dyn.flag.reachJumpingPeak;
		}
		return result;
	}

}

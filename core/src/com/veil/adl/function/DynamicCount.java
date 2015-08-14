package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class DynamicCount implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		String identifier = param[0].toString();
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		int counter = 0;
		for(DynamicEntity dyn : level.getPermanentDynamicEntity()){
			if(dyn.identifier.equalsIgnoreCase(identifier)){
				counter++;
			}
		}
		return counter;
	}

}

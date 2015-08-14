package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class DistanceTo implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		String axis = param[0].toString();
		Position pos = Position.parse(param[1]);
		if(axis.equals("y")){
			return dyn.getWorldCenteredPosition().y - pos.getY();
		}else{
			return dyn.getWorldCenteredPosition().x - pos.getX();
		}
	}

}

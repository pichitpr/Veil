package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class SurfaceInDir implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		Direction dir = Direction.parse(param[0]);
		boolean result = false;
		if(dir.getX() < 0){
			result |= dyn.flag.surfaceInFront[3];
		}else if(dir.getX() > 0){
			result |= dyn.flag.surfaceInFront[1];
		}
		if(dir.getY() < 0){
			result |= dyn.flag.surfaceInFront[2];
		}else if(dir.getY() > 0){
			result |= dyn.flag.surfaceInFront[0];
		}
		return result;
	}

}

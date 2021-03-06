package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;

public class DirectionComponent implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		String component = param[0].toString();
		Direction dir = Direction.parse(param[1]);
		if(component.equalsIgnoreCase("Y"))
			return new Direction(0,dir.getY());
		else if(component.equalsIgnoreCase("X"))
			return new Direction(dir.getX(),0);
		else
			return null;
	}

}

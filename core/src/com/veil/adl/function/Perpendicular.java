package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;

public class Perpendicular implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Direction dir = Direction.parse(param[0]);
		Direction result = new Direction(dir);
		result.rotate(90);
		return result;
	}

}

package com.veil.adl.function;

import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;

public class DirectionSet implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Direction axis = Direction.parse(param[0]);
		Direction opposite = new Direction(axis); opposite.rotate(180);
		List<Direction> dSet = new ArrayList<Direction>();
		dSet.add(axis);
		dSet.add(opposite);
		return dSet;
	}

}

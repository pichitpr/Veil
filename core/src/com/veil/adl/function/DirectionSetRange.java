package com.veil.adl.function;

import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;

public class DirectionSetRange implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Direction start = Direction.parse(param[0]);
		Direction end = Direction.parse(param[1]);
		float angStart = start.getDegree();
		float angEnd = end.getDegree();
		if(angEnd < angStart){
			Direction temp = end;
			end = start;
			start = temp;
			angStart = start.getDegree();
			angEnd = end.getDegree();
		}
		float step = ((Number)param[2]).floatValue();
		List<Direction> dSet = new ArrayList<Direction>();
		while(start.getDegree() <= end.getDegree()){
			dSet.add(new Direction(start));
			start.rotate(step);
		}
		return dSet;
	}

}

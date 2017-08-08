package com.veil.adl.function;

import java.util.ArrayList;
import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;

public class DirectionSetDivide implements IFunction {

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		int slice = ((Number)param[0]).intValue();
		
		//Adjust and cap value to prevent performance hit
		slice = Math.abs(slice);
		if(slice > 100) slice %= 100; //Maximum 100 slices
		if(slice < 1) slice = 1;
		
		float degInc = 360f/slice;
		List<Direction> dSet = new ArrayList<Direction>();
		Direction dir = new Direction();
		for(int i=0; i<slice; i++){
			dSet.add(new Direction(dir));
			dir.rotate(degInc);
		}
		return dSet;
	}

}

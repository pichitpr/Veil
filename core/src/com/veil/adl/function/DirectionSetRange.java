package com.veil.adl.function;

import java.math.BigDecimal;
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
		float step = ((Number)param[2]).floatValue();
		
		float angStart = start.getDegree();
		float angEnd = end.getDegree();
		if(angEnd < angStart){
			angStart -= 360f;
		}
		
		//Adjust value
		if(angEnd < angStart){
			float tmp = angEnd;
			angEnd = angStart;
			angStart = tmp;
		}
		step = Math.abs(step);
		if(step < 1) step = 1f;
		
		//Scale value to prevent performance hit
		if((angEnd - angStart)/step > 100){
			step = (angEnd - angStart)/100f;
		}
		
		List<Direction> dSet = new ArrayList<Direction>();
		BigDecimal bigStart = new BigDecimal(angStart);
		BigDecimal bigEnd = new BigDecimal(angEnd);
		while(bigStart.subtract(bigEnd).compareTo(new BigDecimal(2)) < 0){ //use epsilon
			dSet.add(Direction.parse(""+angStart));
			bigStart = bigStart.add(new BigDecimal(step));
		}

		//Fallback
		if(dSet.size() == 0){
			dSet.add(Direction.parse(""+angStart));
		}
		
		return dSet;
	}

}

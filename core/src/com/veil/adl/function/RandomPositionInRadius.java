package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.MathUtils;
import com.veil.adl.literal.Position;

public class RandomPositionInRadius implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Position pos = Position.parse(param[0]);
		float rad = ((Number)param[1]).floatValue();
		rad = Math.abs(rad);
		if(rad < 1) rad = 1;
		
		float randomX = (float)(pos.getX()-rad+MathUtils.random(rad*2));
		float randomY = (float)Math.sqrt(rad*rad - (pos.getX()-randomX)*(pos.getX()-randomX));
		randomY = (-1f+2f*MathUtils.random(1f))*randomY + pos.getY();
		
		return new Position(randomX,randomY);
	}

}

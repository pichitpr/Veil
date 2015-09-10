package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.MathUtils;
import com.veil.adl.literal.Position;

public class RandomPositionInRange implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Position pos = Position.parse(param[0]);
		Position pos2 = Position.parse(param[1]);
		float tmp;
		if(pos.getX() > pos2.getX()){
			tmp = pos.getX();
			pos.setPosition(pos2.getX(), pos.getY());
			pos2.setPosition(tmp, pos2.getY());
		}
		if(pos.getY() > pos2.getY()){
			tmp = pos.getY();
			pos.setPosition(pos.getX(), pos2.getY());
			pos2.setPosition(pos2.getX(), tmp);
		}
		
		return new Position(
				MathUtils.random(pos.getX(), pos2.getX()), 
				MathUtils.random(pos.getY(), pos2.getY())
				);
	}

}

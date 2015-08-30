package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class Anchor implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Position anchorPos = Position.parse(param[0]);
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		Vector2 dynPos = dyn.getWorldCenteredPosition();
		Direction dir = dyn.direction;
		/*
		if(dir.getX() >= 0){
			return new Position(dynPos.x+anchorPos.getX(),dynPos.y+anchorPos.getY());
		}else if(dir.getX() < 0){
			return new Position(dynPos.x-anchorPos.getX(),dynPos.y+anchorPos.getY());
		}
		return null;
		*/
		if(dir.getX() >= 0){
			return new Position(dynPos.x+anchorPos.getX(),dynPos.y+anchorPos.getY());
		}else{
			return new Position(dynPos.x-anchorPos.getX(),dynPos.y+anchorPos.getY());
		}
	}

}

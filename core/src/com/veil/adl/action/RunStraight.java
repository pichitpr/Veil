package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class RunStraight implements IAction {

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		Direction dir = Direction.parse(param[0]);
		float spd = ((Number)param[1]).floatValue();
		DynamicEntity dyn = itpData.thisAgent; 
		Vector2 vec = new Vector2(dir.getX(),dir.getY());
		vec = vec.nor().scl(spd);
		dyn.addVx(vec.x);
		dyn.addVy(vec.y);
		
		itpData.distance += spd;
		
		return (Boolean)param[2];
	}

	@Override
	public boolean isSpanAction() {
		// TODO Auto-generated method stub
		return true;
	}

}

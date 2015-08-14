package com.veil.adl.action;


import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.level.LevelContainer;

public class AddExtraVelocityToPlayer implements IAction {

	@Override
	public boolean isSpanAction() {
		return true;
	}

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		Direction extraV = Direction.parse(param[0]);
		float spd = ((Number)param[1]).floatValue();
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		
		Vector2 vec =  new Vector2(extraV.getX(),extraV.getY());
		vec.nor().scl(spd);
		level.getPlayer().addVx(vec.x);
		level.getPlayer().addVy(vec.y);
		
		return (Boolean)param[2];
	}

}

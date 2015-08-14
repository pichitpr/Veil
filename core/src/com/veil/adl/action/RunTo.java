package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class RunTo implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		Position pos = Position.parse(param[0]);
		float spd = ((Number)param[1]).floatValue();
		DynamicEntity dyn = itpData.thisAgent;
		
		Vector2 vec = new Vector2(pos.getX()-dyn.getWorldCenteredPosition().x,
				pos.getY()-dyn.getWorldCenteredPosition().y);
		boolean shouldEndAction = vec.len() <= spd;
		if(!shouldEndAction)
			vec = vec.nor().scl(spd);
		dyn.addVx(vec.x);
		dyn.addVy(vec.y);
		
		itpData.distance += spd;
		
		return shouldEndAction;
	}

	@Override
	public boolean isSpanAction() {
		// TODO Auto-generated method stub
		return true;
	}

}

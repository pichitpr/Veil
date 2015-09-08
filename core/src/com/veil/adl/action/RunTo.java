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
		
		boolean shouldEndAction = false;
		if(itp.getSpannedActionInfo().containsKey("RunTo")){
			Vector2 previousPos = (Vector2)itp.getSpannedActionInfo().get("RunTo");
			if(dyn.getWorldCenteredPosition().dst(previousPos) <= 2){
				//If no movement between 2 update -- end action
				shouldEndAction = true;
			}
			
		}
		itp.getSpannedActionInfo().put("RunTo", dyn.getWorldCenteredPosition());
		
		Vector2 vec = new Vector2(pos.getX()-dyn.getWorldCenteredPosition().x,
				pos.getY()-dyn.getWorldCenteredPosition().y);
		if(vec.len() <= spd){
			//If getting close to target point -- end action
			shouldEndAction = true;
		}
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

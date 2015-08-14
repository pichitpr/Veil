package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class RunHarmonic implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		Direction currentDirection;
		float spd = ((Number)param[1]).floatValue();
		DynamicEntity dyn = itpData.thisAgent;
		
		if(!itp.getSpannedActionInfo().containsKey("RunHarmonic")){
			currentDirection = Direction.parse(param[0]);
			itp.getSpannedActionInfo().put("RunHarmonic", currentDirection);
		}else{
			currentDirection = (Direction)itp.getSpannedActionInfo().get("RunHarmonic");
		}
		
		if((Boolean)param[2]){
			currentDirection.rotate(180);
		}
		
		Vector2 vec = new Vector2(currentDirection.getX(),currentDirection.getY());
		vec = vec.nor().scl(spd);
		dyn.addVx(vec.x);
		dyn.addVy(vec.y);
		
		itpData.distance += spd;
		
		return false;
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

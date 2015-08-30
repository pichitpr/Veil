package com.veil.adl.action;

import java.util.List;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.game.element.DynamicEntity;

public class ChangeDirectionToPlayerByStep implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		@SuppressWarnings("unchecked")
		List<Direction> dSet = (List<Direction>)param[0]; //Assumed to be sorted CCW
		int delay = ((Number)param[1]).intValue();
		if(itp.getSpannedActionInfo().containsKey("ChangeDirPLStep")){
			delay = (Integer)itp.getSpannedActionInfo().get("ChangeDirPLStep");
		}
		DynamicEntity dyn = itpData.thisAgent;
		
		if(delay <= 0){
			Direction[] nearest = Direction.getTwoNearestFromSet(dSet, dyn.direction);
			
			Vector2 playerPos = itpData.level.getPlayer().getWorldCenteredPosition();
			Direction toPlayerDir = new Direction(playerPos.x-dyn.getWorldCenteredPosition().x,
					playerPos.y-dyn.getWorldCenteredPosition().y);
			float deltaToPlayer = toPlayerDir.deltaDegree(dyn.direction);
			dyn.direction = new Direction(deltaToPlayer > 0 ? nearest[0] : nearest[1]);
			return true;
		}
		delay--;
		itp.getSpannedActionInfo().put("ChangeDirPLStep",delay);
		return false;
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

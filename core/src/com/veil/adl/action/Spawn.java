package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.adl.literal.Position;
import com.veil.game.element.ScriptedEntity;
import com.veil.game.level.LevelContainer;

public class Spawn implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		if(!level.canHandleMoreEntity()){
			return true;
		}
		String identifier = param[0].toString();
		Position spawnPos = Position.parse(param[1]);
		Direction initDir = null;
		if(param.length > 2)
			initDir = Direction.parse(param[2]);
		
		ScriptedEntity dyn = new ScriptedEntity(level, identifier);
		dyn.setWorldCenteredPosition(spawnPos.getX(), spawnPos.getY());
		if(initDir != null)
			dyn.direction = new Direction(initDir);
		level.pendingSpawn(dyn);
		
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

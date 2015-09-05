package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Direction;
import com.veil.adl.literal.Position;
import com.veil.game.level.LevelContainer;

public class AnchorPlayer implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		Position anchorPos = Position.parse(param[0]);
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		Vector2 player = level.getPlayer().getWorldCenteredPosition();
		Direction dir = level.getPlayer().direction;
		if(dir.getX() >= 0){
			return new Position(player.x+anchorPos.getX(),player.y+anchorPos.getY());
		}else{
			return new Position(player.x-anchorPos.getX(),player.y+anchorPos.getY());
		}
	}

}

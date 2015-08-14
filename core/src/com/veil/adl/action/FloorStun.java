package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.level.LevelContainer;

public class FloorStun implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		int duration = ((Number)param[0]).intValue();
		LevelContainer level = ((ItpData)itp.getAdditionalData()).level;
		level.getPlayer().floorStun(duration);
		return true;
	}

	@Override
	public boolean isSpanAction() {
		return false;
	}

}

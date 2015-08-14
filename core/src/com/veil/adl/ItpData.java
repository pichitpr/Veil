package com.veil.adl;

import adl_2daa.AgentModelInterpreter;
import adl_2daa.InterpreterExtraData;

import com.veil.game.element.ScriptedEntity;
import com.veil.game.level.LevelContainer;

public class ItpData implements InterpreterExtraData {

	public AgentModelInterpreter mainItp; //Main interpreter managing this sequence itp
	public ScriptedEntity thisAgent; //The entity in the game world handled by mainItp
	public LevelContainer level; //The game world the entity resides
	public int frameCounter; //Frame count for current spanned action
	public int distance; //Traveled distance;
	
	public ItpData(AgentModelInterpreter mainItp, ScriptedEntity thisAgent, LevelContainer level){
		this.mainItp = mainItp;
		this.thisAgent = thisAgent;
		this.level = level;
		this.frameCounter = 0;
		this.distance = 0;
	}

	@Override
	public void onInterpreterPause() {
		frameCounter++;
	}

	@Override
	public void onInterpreterReset() {
		frameCounter = 0;
		distance = 0;
	}

	@Override
	public void onLastInstructionInterpreted() {
		frameCounter = 0;
	}

	@Override
	public void onSpannedActionEnd() {
		frameCounter = 0;
		distance = 0;
	}

}

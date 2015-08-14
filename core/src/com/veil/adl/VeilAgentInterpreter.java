package com.veil.adl;

import adl_2daa.AgentModel;
import adl_2daa.AgentModelInterpreter;
import adl_2daa.InterpreterExtraData;

import com.veil.game.element.ScriptedEntity;
import com.veil.game.level.LevelContainer;

public class VeilAgentInterpreter extends AgentModelInterpreter {

	private ScriptedEntity controlledAgent;
	private LevelContainer level;
	
	public VeilAgentInterpreter(AgentModel theAgentModel, ScriptedEntity theEntity,
			LevelContainer theLevel) {
		super(theAgentModel);
		this.controlledAgent = theEntity;
		this.level = theLevel;
	}

	@Override
	protected InterpreterExtraData createInterpreterExtraData() {
		return new ItpData(this, controlledAgent, level);
	}

}

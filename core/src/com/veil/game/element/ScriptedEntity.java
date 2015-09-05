package com.veil.game.element;

import adl_2daa.AgentModelInterpreter;

import com.badlogic.gdx.math.Rectangle;
import com.veil.adl.AgentDatabase;
import com.veil.adl.VeilAgentInterpreter;
import com.veil.game.level.LevelContainer;

public class ScriptedEntity extends DynamicEntity{
	
	private AgentModelInterpreter interpreter;
	
	public ScriptedEntity(LevelContainer level, String identifier){
		super(level, new Rectangle(0,0,1,1), 1);
		this.identifier = identifier;
		this.interpreter = new VeilAgentInterpreter(AgentDatabase.getAgentModelFor(identifier),
				this, level);
		this.interpreter.onSpawn();
	}
	
	/*
	 * public ScriptedEntity(LevelContainer level, Rectangle collider, int texture) {
		super(level, collider, texture);
	}
	*/
	
	/*
	public ScriptedEntity(LevelContainer level, Rectangle collider, int texture, 
			ScriptedEntity parent, boolean useRelativePos, boolean parentPart){
		super(level, collider, texture, parent, useRelativePos, parentPart);
	}
	*/

	@Override
	public void behaviorUpdate(float delta) {
		interpreter.update(delta);
	}

	@Override
	public void postBehaviorUpdate(float delta) {
		interpreter.postUpdate();
		vx = 0;
		vy = 0;
	}

	@Override
	public void handleCollisionEvent() {
		//Handle as defender
		if(this.defender){
			DynamicEntity dyn;
			for(Entity e : flag.collidingEntity){
				if(e instanceof DynamicEntity){
					flag.collideDynamic = true;
					dyn = (DynamicEntity)e;
					if(dyn.attacker){
						if(dyn.group == Group.ENEMY && this.group == Group.HOSTILE){
							continue;
						}
						if(dyn.group == this.group && dyn.group != Group.HOSTILE){
							continue;
						}
						if(!this.invul){
							dyn.flag.damage = true;
							this.hp -= dyn.atk;
							if(this.hp < 0)
								this.hp = 0;
						}else{
							sfxReflect.play(1.f);
						}
						
						if(dyn.projectile && !dyn.invul){
							dyn.hp -= 1;
							if(dyn.hp < 0)
								dyn.hp = 0;
						}
						
						dyn.flag.attack = true;
						flag.attacked = true;
					}
				}
			}
		}
		
		if(gravityEff > 0){
			if(lastVy > 0 && gravityVy+vy < 0) flag.reachJumpingPeak = true;
		}else if(gravityEff < 0){
			if(lastVy < 0 && gravityVy+vy > 0) flag.reachJumpingPeak = true;
		}
		if(lastHP < getBaseHP())
			flag.damaged = true;
	}
	
	@Override
	public void onDespawn(float delta){
		interpreter.onDespawn();
		interpreter = null;
		//hierarchy = null;
		children.clear();
		if(parent != null){
			parent.children.remove(this);
		}
	}
}

package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class Get implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		String stat = param[0].toString();
		DynamicEntity dyn = (DynamicEntity)param[1];		
		if(stat.equalsIgnoreCase("atk")){
			return dyn.atk;
		}else if(stat.equalsIgnoreCase("group")){
			return dyn.group.ordinal();
		}else if(stat.equalsIgnoreCase("direction")){
			return dyn.direction;
		}else if(stat.equalsIgnoreCase("position")){
			return new Position(dyn.getWorldCenteredPosition().x,dyn.getWorldCenteredPosition().y);
		}else if(stat.equalsIgnoreCase("gravityeff")){
			return dyn.getGravityEff();
		}else if(stat.equalsIgnoreCase("collider")){
			//TODO: Change to abstract coordinate system version
			return dyn.getWorldCollider();
		}else if(stat.equalsIgnoreCase("attacker")){
			return dyn.attacker;
		}else if(stat.equalsIgnoreCase("defender")){
			return dyn.defender;
		}else if(stat.equalsIgnoreCase("invul")){
			return dyn.invul;
		}else if(stat.equalsIgnoreCase("projectile")){
			return dyn.projectile;
		}else if(stat.equalsIgnoreCase("phasing")){
			return dyn.phasing;
		}else if(stat.equalsIgnoreCase("parent")){
			return dyn.parent;
		}else if(stat.equalsIgnoreCase("children")){
			return dyn.children;
		}else{ //HP
			return dyn.getBaseHP();
		}
	}

}

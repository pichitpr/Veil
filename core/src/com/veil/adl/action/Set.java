package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.literal.Direction;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.DynamicEntity.Group;

public class Set implements IAction {

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		String stat = param[0].toString();
		DynamicEntity dyn = (DynamicEntity)param[1];
		Object val = param[2];
		
		if(stat.equalsIgnoreCase("atk")){
			dyn.atk = ((Number)val).intValue();
		}else if(stat.equalsIgnoreCase("group")){
			int ordinal = ((Number)val).intValue();
			if(ordinal >= 0 && ordinal < Group.values().length)
				dyn.group = Group.values()[ordinal];
		}else if(stat.equalsIgnoreCase("direction")){
			dyn.direction = Direction.parse(val);
		}else if(stat.equalsIgnoreCase("position")){
			Position pos = Position.parse(val);
			dyn.setWorldCenteredPosition(pos.getX(), pos.getY());
		}else if(stat.equalsIgnoreCase("gravityeff")){
			dyn.setGravityEff(((Number)val).floatValue());
		}else if(stat.equalsIgnoreCase("collider")){
			String[] info = val.toString().split(",");
			dyn.setRectangle(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
		}else if(stat.equalsIgnoreCase("attacker")){
			dyn.attacker = (Boolean)val;
		}else if(stat.equalsIgnoreCase("defender")){
			dyn.defender = (Boolean)val;
		}else if(stat.equalsIgnoreCase("invul")){
			dyn.invul = (Boolean)val;
		}else if(stat.equalsIgnoreCase("projectile")){
			dyn.projectile = (Boolean)val;
		}else if(stat.equalsIgnoreCase("phasing")){
			dyn.phasing = (Boolean)val;
		}else if(stat.equalsIgnoreCase("texture")){
			dyn.setTexture(((Number)val).intValue());
		}else{ //HP
			dyn.setBaseHP(((Number)val).intValue());
		}
		
		return true;
	}

	@Override
	public boolean isSpanAction() {
		// TODO Auto-generated method stub
		return false;
	}

}

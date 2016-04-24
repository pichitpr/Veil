package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;
import com.veil.game.level.LevelContainer;

public class Jump implements IAction{

	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		Position pos = Position.parse(param[0]);
		float maxHeight = ((Number)param[1]).floatValue(); //Must be positive
		//float spd = ((Number)parameter[2].value).floatValue();
		DynamicEntity dyn = itpData.thisAgent;
		LevelContainer level = itpData.level;
		
		//TODO: Pay attention with condition checking during the first update of jumping
		//Since the flag (like InTheAir , SurfaceInFront) is not properly set at this point!
		if(!itp.getSpannedActionInfo().containsKey("Jump")){
			
			/*
			System.out.println(
					"s:"+dyn.getCenteredPosition().x+","+dyn.getCenteredPosition().y+" | "+
					"t:"+pos.getX()+","+pos.getY()+" | "+
					"h:"+maxHeight+" | "+
					"g:"+(-dyn.getGravityEff()*level.getGravity()));
			*/	
			
			//acc vector g
			float g = -dyn.getGravityEff()*level.getGravity();
			if(g > 0) maxHeight = -maxHeight;
			
			//distance vector dy
			float dy = pos.getY()-dyn.getWorldCenteredPosition().y;
			//dy may exceed specified maxHeight
			if((g > 0 && dy < maxHeight) || (g < 0 && dy > maxHeight)){
				maxHeight = dy;
			}
			
			//velocity vector vy
			float vy = (float)Math.sqrt(2.0*Math.abs(g*maxHeight));
			if(g > 0) vy = -vy;
			
			
			float t = (float)( -vy+Math.sqrt(vy*vy+2.0*g*dy) )/g;
			float t2 = (float)( -vy-Math.sqrt(vy*vy+2.0*g*dy) )/g;
			if(t < t2) t = t2; 
			
			
			float vx = (pos.getX()-dyn.getWorldCenteredPosition().x)/t;
			
			dyn.setVx(vx);
			dyn.addGravityVy(vy);
			dyn.flag.jumping = true;
			itp.getSpannedActionInfo().put("Jump",vx);
			
			return false;
		}else{
			dyn.setVx((Float)itp.getSpannedActionInfo().get("Jump"));
		}
		
		//return (Boolean)parameter[3].value;
		return (Boolean)param[3];
	}

	@Override
	public boolean isSpanAction() {
		// TODO Auto-generated method stub
		return true;
	}

}

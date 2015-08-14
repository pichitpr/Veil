package com.veil.adl.action;

import adl_2daa.IAction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.Vector2;
import com.veil.adl.ItpData;
import com.veil.adl.literal.Position;
import com.veil.game.element.DynamicEntity;

public class RunCircling implements IAction{

	//TODO: Complete circling
	@Override
	public boolean invoke(SequenceInterpreter itp, Object... param) {
		ItpData itpData = (ItpData)itp.getAdditionalData();
		Position pos = Position.parse(param[0]);
		float rad = ((Number)param[1]).floatValue();
		float degSpd = ((Number)param[2]).floatValue();
		int driftDelay = ((Number)param[3]).intValue();
		DynamicEntity dyn = itpData.thisAgent;
		Vector2 pnt;
		if(!itp.getSpannedActionInfo().containsKey("RunCircling")){
			pnt = new Vector2(rad,0);
			itp.getSpannedActionInfo().put("RunCircling",pnt);
		}else{
			pnt = (Vector2)itp.getSpannedActionInfo().get("RunCircling");
		}
		dyn.setWorldCenteredPosition(pos.getX()+pnt.x, pos.getY()+pnt.y);
		pnt.rotate(degSpd);
		/*
		Position lastPos = (Position)itp.varLookupTable.get("RunCircling");
		boolean moving = false;
		if(itp.varLookupTable.get("RunCircling_move") != null)
			moving = (Boolean)itp.varLookupTable.get("RunCircling_move");
		
		if(lastPos == null || !lastPos.equals(pos)){
			//Center change
			itp.varLookupTable.put("RunCircling", new Position(pos));
			itp.varLookupTable.put("RunCircling_move", true);
		}else if(moving){
			//Center settled, still moving
			
		}else{
			//Circling
			Vector2 centerToDyn = new Vector2(dyn.getCenteredPosition().x-pos.getX(),
					dyn.getCenteredPosition().y-pos.getY());
			centerToDyn.rotate(degSpd);
			dyn.setCenteredPosition(pos.getX()+centerToDyn.x, pos.getY()+centerToDyn.y);
		}
		*/
		
		/*
		Vector2 dynPos = dyn.getCenteredPosition();
		float distanceFromPath;
		//System.out.println(dynPos.x+","+dynPos.y);
		
		//We need center to dyn vector for circling calculation
		Vector2 vec = new Vector2(dynPos.x-pos.getX(), dynPos.y-pos.getY());
		distanceFromPath = Math.abs(vec.len()-rad);
		if(distanceFromPath > 1){
			System.out.println("AAA");
			if(driftDelay >= 1){
				distanceFromPath /= (1f*driftDelay);
			}
			//Move dyn to EAST if dyn is at the circling center
			if(vec.len() == 0){
				vec = new Vector2(-1,0);
			}
			vec = vec.nor().scl(distanceFromPath);
			itp.distance += vec.len();
		}else{
			System.out.println("BBB");
			vec = vec.rotate(degSpd);
			itp.distance += Math.PI*2f*rad*degSpd/360f;
		}
		vec = vec.rotate(180);
		dyn.setCenteredPosition(dynPos.x+vec.x, dynPos.y+vec.y);
		*/
		return false;
	}

	@Override
	public boolean isSpanAction() {
		return true;
	}

}

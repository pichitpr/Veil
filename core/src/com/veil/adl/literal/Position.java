package com.veil.adl.literal;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Position {

	private Vector2 data;
	
	public Position(){
		data = new Vector2(0,0);
	}
	
	public Position(Position pos){
		data = new Vector2(pos.data);
	}
	
	public Position(float x,float y){
		data = new Vector2(x,y);
	}
	
	public void setPosition(float x,float y){
		data = new Vector2(x,y);
	}
	 
	public void setPositionPolar(float r,float degree){
		data = new Vector2(r*MathUtils.cosDeg(degree),r*MathUtils.sinDeg(degree));
	}
	
	public float getX(){
		return data.x;
	}
	
	public float getY(){
		return data.y;
	}
	
	@Override
	public String toString(){
		return "Pos : "+data.x+","+data.y;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Position)
			return data.equals(((Position)o).data);
		return false;
	}
	
	public static Position parse(Object o){
		if(o instanceof Position)
			return new Position((Position)o);
		String str = o.toString();
		float posX,posY;
		posX = Float.parseFloat(str.substring(2, str.indexOf(',')) );
		posY = Float.parseFloat(str.substring(str.indexOf(',')+1, str.length()-1) );
		Position pos = new Position();
		if(str.charAt(0) == 'p'){
			pos.setPositionPolar(posX, posY);
		}else{
			pos.setPosition(posX, posY);
		}
		return pos;
	}
}

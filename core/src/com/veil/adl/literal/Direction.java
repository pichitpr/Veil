package com.veil.adl.literal;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class Direction {

	public static final Direction NORTH = new Direction(0,1);
	public static final Direction EAST = new Direction(1,0);
	public static final Direction SOUTH = new Direction(0,-1);
	public static final Direction WEST = new Direction(-1,0);
	public static final Direction V = new Direction(NORTH);
	public static final Direction H = new Direction(EAST);
	
	private Vector2 data; 
	
	public Direction(){
		data = new Vector2(EAST.data.cpy());
	}
	
	public Direction(Direction d){
		data = new Vector2(d.data.cpy());
	}
	
	public Direction(float x,float y){
		setDirection(x,y);
	}
	
	public void setDirection(float x,float y){
		data = new Vector2(x,y);
	}
	
	public void setDirectionDegree(float degree){
		data = new Vector2(EAST.data).rotate(degree);
		filterNoise();
	}
	
	public float getX(){
		return data.x;
	}
	
	public float getY(){
		return data.y;
	}
	
	/**
	 * Rotate "degree" degrees CCW
	 */
	public void rotate(float degree){
		data = data.rotate(degree);
		filterNoise();
	}
	
	public float getDegree(){
		return data.angle();
	}
	
	/**
	 * If delta < 0 , this vector is on the right side (CW) of reference vector
	 * Value capped at [-180,180] 
	 * This is the implementation of missing Vector2#angle(Vector2 ref)
	 */
	public float deltaDegree(Direction ref){
		float deltaDeg = getDegree()-ref.getDegree();
		if(deltaDeg > 180)
			deltaDeg -= 360f;
		else if(deltaDeg < -180)
			deltaDeg += 360f;
		return deltaDeg;
	}
	
	
	private void filterNoise(){
		data.set(
				((int)(data.x*1000))/1000f,
				((int)(data.y*1000))/1000f
				);
	}
	
	@Override
	public String toString(){
		return "Dir: "+data.x+","+data.y+" ("+getDegree()+")";
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Direction)
			return data.epsilonEquals(((Direction)o).data, 0.0001f);
		return false;
	}
	
	public static Direction parse(Object o){
		if(o instanceof Direction)
			return new Direction((Direction)o);
		String str = o.toString();
		if(str.equalsIgnoreCase("north")){
			return new Direction(NORTH);
		}else if(str.equalsIgnoreCase("east")){
			return new Direction(EAST);
		}else if(str.equalsIgnoreCase("south")){
			return new Direction(SOUTH);
		}else if(str.equalsIgnoreCase("west")){
			return new Direction(WEST);
		}else if(str.equalsIgnoreCase("v")){
			return new Direction(V);
		}else if(str.equalsIgnoreCase("h")){
			return new Direction(H);
		}else{
			Direction d = new Direction();
			d.setDirectionDegree(Float.parseFloat(str));
			return d;
		}
	}
	
	public static Direction getNearestFromSet(List<Direction> dSet, Direction dir){
		Direction pickedDir = null;
		float minDeg = 400;
		for(int i=0; i<dSet.size(); i++){
			if(Math.abs(dir.deltaDegree(dSet.get(i))) < minDeg){
				minDeg = Math.abs(dir.deltaDegree(dSet.get(i)));
				pickedDir = dSet.get(i);
			}
		}
		return new Direction(pickedDir);
	}
	
	/**
	 * Return 2 directions from CCW-sorted no-repeated dSet, 
	 * [0] being the nearest CCW, [1] for the nearest CW
	 * If dir equals to one of direction in dSet, that direction is discarded from the set during calculation
	 * If dSet has 1 direction, [0] and [1] return the same Direction obj
	 * if dSet is empty, it returns dir for both CW and CCW 
	 */
	public static Direction[] getTwoNearestFromSet(List<Direction> dSetOri, Direction dir){
		List<Direction> dSet = new ArrayList<Direction>();
		dSet.addAll(dSetOri);
		Direction[] result = new Direction[2];
		
		//Discard duplicated direction
		int skipping = -1;
		for(int i=0; i<dSet.size(); i++){
			if(Math.abs(dSet.get(i).deltaDegree(dir)) < 1f){
				skipping = i;
				break;
			}
		}
		if(dSet.size() == 0 || (dSet.size() == 1 && skipping > -1)){
			result[0] = new Direction(dir);
			result[1] = new Direction(dir);
			return result;
		}
		if(skipping > -1) dSet.remove(skipping);
		
		int nearestIdx = -1;
		float minDeg = 400;
		float delta;
		for(int i=0; i<dSet.size(); i++){
			delta = Math.abs(dir.deltaDegree(dSet.get(i)));
			if(delta < minDeg){
				minDeg = delta;
				nearestIdx = i;
			}
		}
		if(dir.deltaDegree(dSet.get(nearestIdx)) > 0){
			//dir on the left(CCW) of nearest
			result[1] = dSet.get(nearestIdx);
			nearestIdx++;
			if(nearestIdx >= dSet.size()) nearestIdx = 0;
			result[0] = dSet.get(nearestIdx);
		}else{
			//dir on the right
			result[0] = dSet.get(nearestIdx);
			nearestIdx--;
			if(nearestIdx < 0) nearestIdx = dSet.size()-1;
			result[1] = dSet.get(nearestIdx);
		}
		
		return result;
	}
}

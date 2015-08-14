package com.veil.adl.literal;

import java.util.Arrays;
import java.util.Comparator;
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
		data = new Vector2(EAST.data);
	}
	
	public Direction(Direction d){
		data = new Vector2(d.data);
	}
	
	public Direction(float x,float y){
		setDirection(x,y);
	}
	
	public void setDirection(float x,float y){
		data = new Vector2(x,y);
	}
	
	public void setDirectionDegree(float degree){
		data = new Vector2(EAST.data).rotate(degree);
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
	
	@Override
	public String toString(){
		return "Dir: "+data.x+","+data.y;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Direction)
			return data.equals(((Direction)o).data);
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
	 * Return sorted index of Direction, 
	 * the first index is the CW-most to the ref dir, 
	 * the last index is the CCW-most to the ref dir
	 */
	public static Integer[] getSortedDirectionList(final List<Direction> dSet, final Direction dir){
		Integer[] sortedIdx = new Integer[dSet.size()+1];
		for(int i=0; i<dSet.size()+1; i++){
			sortedIdx[i] = i-1;
		}
		Arrays.sort(sortedIdx, new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				float  deg1,deg2;
				if(o1 == -1)
					deg1 = 0;
				else
					deg1 = dSet.get(o1).deltaDegree(dir);
				if(o2 == -1)
					deg2 = 0;
				else
					deg2 = dSet.get(o1).deltaDegree(dir);
				if(deg1 < deg2) return -1;
				else if(deg1 > deg2) return 1;
				return 0;
			}
		});
		return sortedIdx;
	}
}

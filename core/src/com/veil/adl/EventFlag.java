package com.veil.adl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.veil.game.element.Entity;

public class EventFlag {
	
	//NORTH , EAST , SOUTH , WEST
	public boolean[] surfaceInFront = new boolean[4];
	public boolean[] screenEdgeInFront = new boolean[4];
	
	public boolean reachJumpingPeak = false;
	public boolean damaged = false,attacked = false;
	public boolean damage = false, attack = false;
	public boolean collideDynamic = false;
	public HashSet<Entity> collidingEntity = new HashSet<Entity>();
	
	private List<Integer> activeMessage = new ArrayList<Integer>();
	private List<Integer> pendingMessage = new ArrayList<Integer>();
	
	public List<Integer> getMessageTable(){
		return activeMessage;
	}
	
	public void notify(int message){
		pendingMessage.add(message);
	}
	
	public void clear(){
		for(int i=0; i<4; i++){
			surfaceInFront[i] = false;
			screenEdgeInFront[i] = false;
		}
		reachJumpingPeak = false;
		damaged = false;
		attacked = false;
		damage = false;
		attack = false;
		collideDynamic = false;
		collidingEntity.clear();
		
		List<Integer> tmp = activeMessage;
		activeMessage = pendingMessage;
		pendingMessage = tmp;
		pendingMessage.clear();
	}
	
	public boolean jumping = false;
}


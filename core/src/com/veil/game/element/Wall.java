package com.veil.game.element;

import com.badlogic.gdx.math.Rectangle;

public class Wall extends Entity{

	public Wall(Rectangle collider, int texture) {
		super(collider, texture);
		// TODO Auto-generated constructor stub
	}
	
	/*
	private int counter = 200;
	@Override
	public void update(float delta){
		if(counter > 100){
			counter--;
			collider.setPosition(collider.x+2, collider.y);
		}else if(counter > 0){
			counter--;
			collider.setPosition(collider.x-2, collider.y);
		}else{
			counter = 200;
		}
	}
	*/
}

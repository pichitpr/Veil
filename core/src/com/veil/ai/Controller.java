package com.veil.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.veil.game.GameConstant;

public class Controller {

	public static Controller instance = new Controller();
	
	public boolean up,down,left,right,jump,shoot;
	
	public void preUpdate(){
		up = false;
		down = false;
		left = false;
		right = false;
		jump = false;
		shoot = false;
		
		if(!GameConstant.useAI){
			if(Gdx.input.isKeyPressed(Input.Keys.UP)){
				up = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				down = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				left = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				right = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.Z)){
				jump = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.X)){
				shoot = true;
			}
		}
	}
}

package com.veil.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.veil.game.GameConstant;

public class Controller {

	public static Controller instance = new Controller();
	
	public boolean up,down,left,right,jump,shoot,pause;
	private com.badlogic.gdx.controllers.Controller extController = null;
	
	public void setup(){
		extController = null;
		if(Controllers.getControllers().size > 0){
			extController = Controllers.getControllers().get(0);
		}
	}
	
	public void preUpdate(){
		up = false;
		down = false;
		left = false;
		right = false;
		jump = false;
		shoot = false;
		pause = false;
		
		if(!GameConstant.useAI){
			if(GameConstant.profilingMode && !BattleProfile.instance.isStart())
				return;
			if(extController == null){
				controlWithKeyboard();
			}else{
				controlWithExtController();
			}
		}
	}
	
	private void controlWithKeyboard(){
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
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
			pause = true;
		}
	}
	
	private void controlWithExtController(){
		/*
		Logitech
		Left analog: axis2 (-1,1 = left,right)  axis3 (-1,1 = up,down)
		Right analog: axis0 (-1,1 = left,right)  axis1 (-1,1 = up,down)
		0: btnLeft
		1: btnDown
		2: btnRight
		3: btnUp
		4: L1
		5: R1
		6: L2
		7: R2
		8: Select
		9: Start
		*/
		float x = extController.getAxis(3);
		float y = extController.getAxis(2);
		if(x < -0.7f){
			left = true;
		}else if(x > 0.7f){
			right = true;
		}
		if(y < -0.7f){
			up = true;
		}else if(y > 0.7f){
			down = true;
		}
		if(extController.getButton(1)){
			jump = true;
		}
		if(extController.getButton(0)){
			shoot = true;
		}
		if(extController.getButton(9)){
			pause = true;
		}
	}
}

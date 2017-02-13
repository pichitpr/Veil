package com.veil.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.veil.game.GameConstant;

public class Controller {

	public static Controller instance = new Controller();
	
	public boolean up,down,left,right,jump,shoot,pause;
	private com.badlogic.gdx.controllers.Controller extController = null;
	private boolean extControllerPauseLastFrame = false;
	
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
	
	//Bandaid code for InfoScene
	public void preUpdateInfoScene(){
		up = false;
		down = false;
		left = false;
		right = false;
		jump = false;
		shoot = false;
		pause = false;
		if(!GameConstant.useAI){
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
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
		
		PovDirection controllerPOV = controller.getPov(0);
		if(PovDirection == PovDirection.north)
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
		/*
		PovDirection controllerPOV = extController.getPov(0);
		switch(controllerPOV){
		case north: up = true; break;
		case east: right = true; break;
		case south: down = true; break;
		case west: left = true; break;
		case northEast: right = true; up = true; break;
		case northWest: left = true; up = true; break;
		case southEast: right = true; down = true; break;
		case southWest: left = true; down = true; break;
		default:
		}
		*/
		if(extController.getButton(1)){
			jump = true;
		}
		if(extController.getButton(0)){
			shoot = true;
		}
		if(extController.getButton(9)){
			if(!extControllerPauseLastFrame){
				extControllerPauseLastFrame = true;
				pause = true;
			}
		}else{
			extControllerPauseLastFrame = false;
		}
	}
	
	public boolean isUsingController(){
		return extController != null;
	}
}

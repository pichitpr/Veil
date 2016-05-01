package com.veil.ai;


public class GameAI {
	
	public static GameAI instance = new GameAI();

	private int shootCounter = 0;
	private boolean walkLeft = false;
	private int walkCounter = 0;
	private boolean holdJump = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info){
		preControlUpdate(controller, info);
	}
	
	private void preControlUpdate(Controller controller, LevelSnapshot info){
		if(walkCounter > 0){
			walkCounter--;
		}
		if(shootCounter > 0){
			shootCounter--;
		}
	}
	
	private void walk(Controller controller, boolean left, int steps){
		walkCounter = steps;
		if(walkCounter > 0){
			if(walkLeft){
				controller.left = true;
			}else{
				controller.right = true;
			}
		}
	}
	
	private void shoot(Controller controller, int frequency){
		if(shootCounter == 0){
			controller.shoot = true;
			shootCounter = frequency+1; //We need at least 1 frame to release button 
		}
	}
	
	private void jump(Controller controller, LevelSnapshot info){
		if(holdJump){
			if(info.playerState.reachJumpingPeak){
				holdJump = false;
			}
		}else{
			if(info.playerOnFloor){
				holdJump = true;
			}
		}
		if(holdJump){
			controller.jump = true;
		}
	}
}

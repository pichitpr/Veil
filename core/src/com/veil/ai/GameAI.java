package com.veil.ai;


public class GameAI {
	
	public static GameAI instance = new GameAI();

	private boolean holdJump = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info){
		jump(controller, info);
		if(holdJump){
			controller.jump = true;
		}
	}
	
	private void jump(Controller controller, LevelSnapshot info){
		if(holdJump){
			if(info.playerState.reachJumpingPeak){
				holdJump = false;
			}
		}else{
			if(info.onFloor){
				holdJump = true;
			}
		}
	}
}

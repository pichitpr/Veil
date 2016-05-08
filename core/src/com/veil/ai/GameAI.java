package com.veil.ai;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Rectangle;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;


public class GameAI {
	
	public static GameAI instance = new GameAI();
	
	//For debugging purpose
	public static Rectangle[] predictedEnemyPos;
	
	//Human parameter
	private int buttonSpamDelay; //A number of frame delay for pressing shoot button (min 2)
	//private int reactionTime; //A number of frame required to predict any agent 
	
	private int historyBufferSize = 8;
	
	private HashMap<DynamicEntity, FrameHistoryBuffer> entityTracker = new HashMap<DynamicEntity, FrameHistoryBuffer>();
	
	private int goalX, goalY;
	private boolean pressJump = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info){
		//Setup frame data for prediction
		setupFrameData(info);
		//Set goal based on predicted enemy path
		setGoal(info);
		//Move according to goal
		moveToGoal(controller, goalX, goalY, info.playerRect, !info.playerState.surfaceInFront[2]);
	}
	
	private void setupFrameData(LevelSnapshot info){
		for(FrameHistoryBuffer buf : entityTracker.values()){
			buf.clearUpdateFlag();
		}
		if(info.enemy != null){
			if(!entityTracker.containsKey(info.enemy)){
				entityTracker.put(info.enemy, new FrameHistoryBuffer(historyBufferSize));
			}
			entityTracker.get(info.enemy).addFrame(info.enemyRect);
		}
		for(Entry<DynamicEntity, Rectangle> entry : info.tempRect.entrySet()){
			if(!entityTracker.containsKey(entry.getKey())){
				entityTracker.put(entry.getKey(), new FrameHistoryBuffer(historyBufferSize));
			}
			entityTracker.get(entry.getKey()).addFrame(entry.getValue());
		}
		//Remove obsolete entities
		entityTracker.entrySet().removeIf( e -> !e.getValue().isUpdated() );
	}
	
	private static final boolean[][] buttonCombination = {
		{false, false, false},
		{true, false, false},
		{false, true, false},
		{false, false, true},
		{true, false, true},
		{false, true, true}
	};
	
	private void setGoal(LevelSnapshot info){
		if(info.enemy != null && entityTracker.containsKey(info.enemy)){
			predictedEnemyPos = entityTracker.get(info.enemy).predictNextFrame(10);
		}
		int lowestCombinationCost = Integer.MAX_VALUE;
		int combination = -1;
		for(int i=0; i<buttonCombination.length; i++){
			Rectangle player = simulatePlayerPlatformer(info, 
					buttonCombination[i][0], buttonCombination[i][1], buttonCombination[i][2]);
			int cost = 0;
			for(FrameHistoryBuffer buf : entityTracker.values()){
				Rectangle[] enemyRects = buf.predictNextFrame(20);
				for(Rectangle enemy : enemyRects){
					if(enemy.overlaps(player)){
						cost++;
					}
				}
			}
			if(cost < lowestCombinationCost){
				System.out.println(cost);
				lowestCombinationCost = cost;
				combination = i;
			}
		}
		Rectangle goalRect = simulatePlayerPlatformer(info, buttonCombination[combination][0], 
				buttonCombination[combination][1], buttonCombination[combination][2]);
		goalX = Math.round(goalRect.x);
		goalY = Math.round(goalRect.y);
	}
	
	private void moveToGoal(Controller controller,int goalX, int goalY, Rectangle playerRect, boolean playerInAir){
		int distX = goalX - (int)playerRect.x;
		int distY = goalY - (int)playerRect.y;
		if(Math.abs(distX) > GameConstant.speed){
			if(distX < 0){
				controller.left = true;
			}else{
				controller.right = true;
			}
		}
		if(distY > 0){
			if(!playerInAir){
				//Release the button and try to jump again
				if(pressJump){
					pressJump = false;
				}else{
					controller.jump = true;
					pressJump = true;
				}
			}else{
				//Do not release jump button if goal is above
				controller.jump = true; 
			}
		}
	}
	
	private Rectangle simulatePlayerPlatformer(LevelSnapshot info, boolean left, boolean right, boolean jump){
		Rectangle rect = new Rectangle(info.playerRect);
		float[] simulatedVelocity = info.player.simulateMovement(left, right, false, false, jump);
		rect.x += simulatedVelocity[0];
		rect.y += simulatedVelocity[1]+simulatedVelocity[2];
		return rect;
	}
}

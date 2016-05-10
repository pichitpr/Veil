package com.veil.ai;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Rectangle;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;


public class GameAI {
	
	public static GameAI instance = new GameAI();
	
	//For debugging purpose
	public static Rectangle[] simulatedPlayerPos;
	public static Rectangle[][] predictedEnemyPos;
	public static Rectangle[] bufferedEnemyPos;
	
	//Human parameter
	private int buttonSpamDelay; //A number of frame delay for pressing shoot button (min 2)
	//private int reactionTime; //A number of frame required to predict any agent 
	
	private int historyBufferSize = 5;
	
	private HashMap<DynamicEntity, FrameHistoryBuffer> entityTracker = new HashMap<DynamicEntity, FrameHistoryBuffer>();
	
	private int goalX, goalY;
	private boolean pressJump = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info, float delta){
		//Setup frame data for prediction
		setupFrameData(info);
		//Set goal based on predicted enemy path
		simulateAndSetGoal(info, delta);
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
				entityTracker.get(info.enemy).verbose = true;
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
	
	private void simulateAndSetGoal(LevelSnapshot info, float delta){
		Rectangle[][] predictedFrames = new Rectangle[entityTracker.size()][];
		predictedEnemyPos = new Rectangle[entityTracker.size()][];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			predictedFrames[index] = entityTracker.get(dyn).predictNextFrame(20);
			predictedEnemyPos[index] = predictedFrames[index];
			if(dyn == info.enemy){
				bufferedEnemyPos = new Rectangle[entityTracker.get(dyn).getBuffer().size()];
				int i=0;
				for(Rectangle rect : entityTracker.get(dyn).getBuffer()){
					bufferedEnemyPos[i] = rect;
					i++;
				}
			}
			index++;
		}
		int lowestCombinationCost = Integer.MAX_VALUE;
		int combination = -1;
		for(int i=0; i<buttonCombination.length; i++){
			Rectangle player = simulatePlayerPlatformer(info, 
					buttonCombination[i][0], buttonCombination[i][1], buttonCombination[i][2], delta);
			int cost = 0;
			index = 0;
			for(DynamicEntity dyn : entityTracker.keySet()){
				for(Rectangle enemy : predictedFrames[index]){
					if(enemy.overlaps(player)){
						cost++;
					}
				}
				index++;
			}
			if(cost < lowestCombinationCost){
				lowestCombinationCost = cost;
				combination = i;
			}
		}
		Rectangle goalRect = simulatePlayerPlatformer(info, buttonCombination[combination][0], 
				buttonCombination[combination][1], buttonCombination[combination][2], delta);
		goalX = Math.round(goalRect.x);
		goalY = Math.round(goalRect.y);
		
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		Rectangle[] rightJumpPlayer = dummy.simulatePosition(info.player, false, true, false, false, true, 40, delta);
		simulatedPlayerPos = new Rectangle[rightJumpPlayer.length/4];
		for(int i=0; i<simulatedPlayerPos.length; i++){
			simulatedPlayerPos[i] = rightJumpPlayer[i*4+3];
		}
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
	
	private Rectangle simulatePlayerPlatformer(LevelSnapshot info, boolean left, boolean right, boolean jump, float simDelta){
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		return dummy.simulatePosition(info.player, left, right, false, false, jump, 50, simDelta)[20];
	}
}

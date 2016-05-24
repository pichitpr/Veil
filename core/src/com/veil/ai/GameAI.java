package com.veil.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;


public class GameAI {
	
	public static GameAI instance = new GameAI();
	
	//For debugging purpose
	public static Rectangle[] simulatedPlayerPos;
	public static Rectangle[][] predictedEnemyPos;
	public static Rectangle[] bufferedEnemyPos;
	
	//Parameter
	private int buttonSpamDelay; //A number of frame delay for pressing shoot button (min 2)
	private int reactionTime = 4; //A number of frame required to re-decide button press 
	private int historyBufferSize = 5;
	
	private HashMap<DynamicEntity, FrameHistoryBuffer> entityTracker = new HashMap<DynamicEntity, FrameHistoryBuffer>();
	
	private int goalX, goalY;
	private boolean pressJump = false;
	private boolean startFallingDown = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info, float delta){
		//Setup frame data for prediction
		setupFrameDataAndFlag(info);
		searchButtonCombination(info, delta);
		pressButton(controller, info, delta);
		/*
		//Set goal based on predicted enemy path
		simulateAndSetGoal(info, delta);
		//Move according to goal
		moveToGoal(controller, goalX, goalY, info.playerRect, !info.playerState.surfaceInFront[2]);
		*/
	}
	
	private void setupFrameDataAndFlag(LevelSnapshot info){
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
		
		//Setup player's jumping flag
		if(info.playerState.reachJumpingPeak){
			startFallingDown = true;
		}else if(info.playerState.surfaceInFront[2]){
			startFallingDown = false;
		}
	}
	
	private static final boolean[][] buttonCombination = {
		{false, false, false},
		{true, false, false},
		{false, true, false},
		{false, false, true},
		{true, false, true},
		{false, true, true}
	};
	
	private int currentCombination = 0;
	private int simulationDepth = 3;
	private int safeMargin = 3;
	
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[][] predictedFrames = new Rectangle[entityTracker.size()][];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			predictedFrames[index] = entityTracker.get(dyn).predictNextFrame(reactionTime*simulationDepth + safeMargin);
			index++;
		}
		predictedEnemyPos = predictedFrames;
		
		int minCost = Integer.MAX_VALUE;
		List<Integer> selectedCombination = new ArrayList<Integer>();
		//Loop through all possible button press
		for(int i=0; i<buttonCombination.length; i++){
			//Calculate cost for the combination and pick the least one
			boolean[] btn = buttonCombination[i];
			int combinationCost = 0;
			int futureFrame;
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(info.player);
			
			//Cost calculation and combination selection
			//- Cost increased if "future frame" (the next frame that AI could change button press) is NOT SAFE within specified
			// safe margin --- The frame is deemed safe if its bound does not collide with all bounds in R; where R contains all
			// enemies' bound at the frame [futureFrame-safeMargin, futureFrame+safeMargin]
			//			Cost increased by 1 for each collision
			//- Cost increased if "next future frame" (the next N frame (N > 1) that AI could change button press) is NOT SAFE.
			//			Cost increased by C; C = min(All possible future frame cost)
			futureFrame = reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn[0], btn[1], false, false, btn[2], 
					reactionTime, delta)[futureFrame];
			for(Rectangle[] frame : predictedFrames){
				for(int j=futureFrame-safeMargin; j<=futureFrame+safeMargin; j++){
					if(j < 0 || j >= frame.length) continue;
					if(frame[j].overlaps(nextPlayerFrame)){
						combinationCost++;
					}
				}
			}
			 combinationCost += searchMinFutureCost(info, delta, predictedFrames, dummy, 1);
			 System.out.println(i+" : "+combinationCost);
			 
			 if(combinationCost < minCost){
				 minCost = combinationCost;
				 selectedCombination.clear();
				 selectedCombination.add(i);
			 }else if(combinationCost == minCost){
				 selectedCombination.add(i);
			 }
		}
		
		//There is combination with equal cost, try to retain moving direction
		if(selectedCombination.size() > 1){
			List<Integer> retained = new ArrayList<Integer>();
			//Prioritize non-jumping action + same direction > Else. Combination criteria are as following:
			//- Check if jump button provide no benefit (during fall down, jump button do nothing)
			//-- True:
			//----- If combination direction is the same as previous one, select that combination WITHOUT jumping
			//-- False:
			//----- If combination is the same as previous one: Copy it
			//----- Otherwise, retain direction
			for(int comb : selectedCombination){
				if(startFallingDown){
					//Exclude jumping choice during fall down but direction must be retained
					if((buttonCombination[comb][0] && buttonCombination[currentCombination][0]) || 
							(buttonCombination[comb][1] && buttonCombination[currentCombination][1])){
						if(buttonCombination[comb][2]){
							//Has jumping, save non-jumping version combination instead
							retained.add(comb-3);
						}else{
							retained.add(comb);
						}
					}
				}else{
					//Prioritize previous combination
					if(comb == currentCombination){
						return;
					}
					//Check if the combination can retain moving direction (left-right only)
					if((buttonCombination[comb][0] && buttonCombination[currentCombination][0]) || 
							(buttonCombination[comb][1] && buttonCombination[currentCombination][1])){
						retained.add(comb);
					}
				}
			}
			if(retained.size() > 0){
				currentCombination = retained.get( MathUtils.random(0,retained.size()-1) );
			}else{
				currentCombination = selectedCombination.get( MathUtils.random(0,selectedCombination.size()-1) );
			}
		}else{
			currentCombination = selectedCombination.get(0);
		}
	}
	
	private int searchMinFutureCost(LevelSnapshot info, float delta, Rectangle[][] predictedFrames, 
			DummyPlayer player, int depth){
		int minCost = Integer.MAX_VALUE;
		for(int i=0; i<buttonCombination[i].length; i++){
			boolean[] btn = buttonCombination[i];
			int combinationCost = 0;
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(player);
			int nextfutureFrame = (depth+1)*reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn[0], btn[1], false, false, btn[2], 
					reactionTime, delta)[reactionTime-1];
			for(Rectangle[] frame : predictedFrames){
				for(int j=nextfutureFrame-safeMargin; j<=nextfutureFrame+safeMargin; j++){
					if(j < 0 || j >= frame.length) continue;
					if(frame[j].overlaps(nextPlayerFrame)){
						combinationCost++;
					}
				}
			}
			if(combinationCost > minCost){
				continue;
			}
			
			if(depth < simulationDepth-1)
				combinationCost += searchMinFutureCost(info, delta, predictedFrames, player, depth+1);
			
			if(combinationCost < minCost)
				minCost = combinationCost;
		}
		return minCost;
	}
	
	private void pressButton(Controller controller, LevelSnapshot info, float delta){
		System.out.println("Selected "+currentCombination);
		
		controller.left = buttonCombination[currentCombination][0];
		controller.right = buttonCombination[currentCombination][1];
		//Require jumping, check if jumping should be delayed by 1 frame or not (release jumping button before pressing again)
		if(buttonCombination[currentCombination][2]){
			controller.jump = !startFallingDown;
			/*
			//On floor
			if(info.playerState.surfaceInFront[2]){
				//Release the button and try to jump again next frame
				if(pressJump){
					controller.jump = false;
					pressJump = false;
				}else{
					controller.jump = true;
					pressJump = true;
				}
			}else{
				controller.jump = true; 
			}
			*/
		}
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		dummy.mimicPlayer(info.player);
		Rectangle[] rect = dummy.simulatePosition(controller.left, controller.right, controller.up, controller.down, controller.jump,
						reactionTime, delta);
		simulatedPlayerPos = rect;
	}
	
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
		
		simulatedPlayerPos = new Rectangle[]{ goalRect };
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
		dummy.mimicPlayer(info.player);
		return dummy.simulatePosition(left, right, false, false, jump, reactionTime, simDelta)[reactionTime-1];
	}
}

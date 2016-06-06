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
	
	private HashMap<DynamicEntity, FrameHistoryBuffer> entityTracker = new HashMap<DynamicEntity, FrameHistoryBuffer>();
	
	private boolean startFallingDown = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info, float delta){
		setupFrameDataAndFlag(info);
		if(GameConstant.profilingMode && !BattleProfile.instance.isStart())
			return;
		searchButtonCombination(info, delta);
		pressButton(controller, info, delta);
	}
	
	private void setupFrameDataAndFlag(LevelSnapshot info){
		for(FrameHistoryBuffer buf : entityTracker.values()){
			buf.clearUpdateFlag();
		}
		if(info.enemy != null){
			if(!entityTracker.containsKey(info.enemy)){
				entityTracker.put(info.enemy, new FrameHistoryBuffer());
			}
			entityTracker.get(info.enemy).addFrame(info.enemyRect);
		}
		for(Entry<DynamicEntity, Rectangle> entry : info.tempRect.entrySet()){
			if(!entityTracker.containsKey(entry.getKey())){
				entityTracker.put(entry.getKey(), new FrameHistoryBuffer());
			}
			entityTracker.get(entry.getKey()).addFrame(entry.getValue());
		}
		//Remove obsolete entities
		entityTracker.entrySet().removeIf( e -> !e.getValue().isUpdated() );
		
		//Setup player's jumping flag
		if(info.playerState.reachJumpingPeak){
			startFallingDown = true;
		}else if(info.playerOnFloor){
			startFallingDown = false;
		}
	}
	
	private enum ButtonCombination {
		None(false, false, false),
		Left(true, false, false),
		Right(false, true, false),
		Jump(false, false, true),
		LeftJump(true, false, true),
		RightJump(false, true, true);
		
		private boolean[] pressed; 
		private ButtonCombination(boolean pressLeft, boolean pressRight, boolean pressJump){
			pressed = new boolean[]{pressLeft, pressRight, pressJump};
		}
		
		public boolean leftPressed(){
			return pressed[0];
		}
		
		public boolean rightPressed(){
			return pressed[1];
		}
		
		public boolean jumpPressed(){
			return pressed[2];
		}
		
		public boolean sameMovingDirectionAs(ButtonCombination comb){
			return (comb.leftPressed() && this.leftPressed()) || (comb.rightPressed() && this.rightPressed());
		}
		
		public ButtonCombination nonJumpVersion(){
			if(this == Jump) return None;
			else if(this == LeftJump) return Left;
			else if(this == RightJump) return Right;
			else return this;
		}
	}
	
	private ButtonCombination currentCombination = ButtonCombination.None;
	
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[][] predictedFrames = new Rectangle[entityTracker.size()][];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			predictedFrames[index] = entityTracker.get(dyn).predictNextFrame(
					AIConstant.reactionTime*AIConstant.simulationDepth + AIConstant.safeMargin
					);
			index++;
		}
		predictedEnemyPos = predictedFrames;
		
		int minCost = Integer.MAX_VALUE;
		List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
		//Loop through all possible button press
		for(ButtonCombination btn : ButtonCombination.values()){
			//Calculate cost for the combination and pick the least one
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
			futureFrame = AIConstant.reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), AIConstant.reactionTime, delta)[futureFrame];
			for(Rectangle[] frame : predictedFrames){
				for(int j=futureFrame-AIConstant.safeMargin; j<=futureFrame+AIConstant.safeMargin; j++){
					if(j < 0 || j >= frame.length) continue;
					if(frame[j].overlaps(nextPlayerFrame)){
						combinationCost++;
					}
				}
			}
			 combinationCost += searchMinFutureCost(info, delta, predictedFrames, dummy, 1);
			 //System.out.println(btn+" : "+combinationCost);
			 
			 if(combinationCost < minCost){
				 minCost = combinationCost;
				 selectedCombination.clear();
				 selectedCombination.add(btn);
			 }else if(combinationCost == minCost){
				 selectedCombination.add(btn);
			 }
		}
		
		//There is combination with equal cost, try to retain moving direction
		if(selectedCombination.size() > 1){
			List<ButtonCombination> retained = new ArrayList<ButtonCombination>();
			//Prioritize non-jumping action + same direction > Else. Combination criteria are as following:
			//- Check if jump button provide no benefit (during fall down, jump button do nothing)
			//-- True:
			//----- If combination direction is the same as previous one, select that combination WITHOUT jumping
			//-- False:
			//----- If combination is the same as previous one: Copy it
			//----- Otherwise, retain direction
			for(ButtonCombination comb : selectedCombination){
				if(startFallingDown){
					//Exclude jumping choice during fall down but direction must be retained
					if(comb.sameMovingDirectionAs(currentCombination)){
						retained.add(comb.nonJumpVersion());
					}
				}else{
					//Prioritize previous combination
					if(comb == currentCombination){
						return;
					}
					//Check if the combination can retain moving direction (left-right only)
					if(comb.sameMovingDirectionAs(currentCombination)){
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
		for(ButtonCombination btn : ButtonCombination.values()){
			int combinationCost = 0;
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(player);
			int nextfutureFrame = (depth+1)*AIConstant.reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), AIConstant.reactionTime, delta)[AIConstant.reactionTime-1];
			for(Rectangle[] frame : predictedFrames){
				for(int j=nextfutureFrame-AIConstant.safeMargin; j<=nextfutureFrame+AIConstant.safeMargin; j++){
					if(j < 0 || j >= frame.length) continue;
					if(frame[j].overlaps(nextPlayerFrame)){
						combinationCost++;
					}
				}
			}
			if(combinationCost > minCost){
				continue;
			}
			
			if(depth < AIConstant.simulationDepth-1)
				combinationCost += searchMinFutureCost(info, delta, predictedFrames, player, depth+1);
			
			if(combinationCost < minCost)
				minCost = combinationCost;
		}
		return minCost;
	}
	
	private void pressButton(Controller controller, LevelSnapshot info, float delta){
		//System.out.println("Selected "+currentCombination);
		
		controller.left = currentCombination.leftPressed();
		controller.right = currentCombination.rightPressed();
		//Require jumping, check if jumping should be delayed by 1 frame or not (release jumping button before pressing again)
		if(currentCombination.jumpPressed()){
			controller.jump = !startFallingDown;
		}
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		dummy.mimicPlayer(info.player);
		Rectangle[] rect = dummy.simulatePosition(controller.left, controller.right, controller.up, controller.down, controller.jump,
				AIConstant.reactionTime, delta);
		simulatedPlayerPos = rect;
	}
}

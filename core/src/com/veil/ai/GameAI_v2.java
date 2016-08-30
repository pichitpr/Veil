package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.veil.game.element.DynamicEntity;

public class GameAI_v2 extends GameAI {

	private final int reactionTime = 4;
	private final int simulationDepth = 6;
	private final int safeMargin = 5;
	
	@Override
	protected void onReset(DynamicEntity initialStateEnemy) {
		
	}
	
	@Override
	protected void pressButton(Controller controller, LevelSnapshot info,
			float delta) {
		searchButtonCombination(info, delta);
		pressButtonByCombination(controller, info, delta);
	}
	
	private ButtonCombination currentCombination = ButtonCombination.None;
	
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[][] predictedFrames = new Rectangle[entityTracker.size()][];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			predictedFrames[index] = entityTracker.get(dyn).predictNextFrame(
					reactionTime*simulationDepth + safeMargin
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
			futureFrame = reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), reactionTime, delta)[futureFrame];
			for(Rectangle[] frame : predictedFrames){
				for(int j=futureFrame-safeMargin; j<=futureFrame+safeMargin; j++){
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
			int nextfutureFrame = (depth+1)*reactionTime-1;
			Rectangle nextPlayerFrame = dummy.simulatePosition(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), reactionTime, delta)[reactionTime-1];
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
	
	private void pressButtonByCombination(Controller controller, LevelSnapshot info, float delta){
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
				reactionTime, delta);
		simulatedPlayerPos = rect;
	}
}

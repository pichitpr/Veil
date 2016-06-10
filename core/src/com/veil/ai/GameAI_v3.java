package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.veil.game.element.DynamicEntity;

public class GameAI_v3 extends GameAI {

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
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(info.player);
			Rectangle[] playerFutures = dummy.simulatePosition(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), AIConstant.reactionTime*AIConstant.simulationDepth, delta);
			for(int frameRef = 0; frameRef<playerFutures.length; frameRef++){
				int currentDepth = frameRef/AIConstant.simulationDepth;
				int frameCost = (int)Math.pow(2, AIConstant.simulationDepth-1-currentDepth);
				for(Rectangle[] frame : predictedFrames){
					boolean collideWithEnemy = false;
					for(int j=frameRef-AIConstant.safeMargin; j<=frameRef+AIConstant.safeMargin; j++){
						if(j < 0 || j >= frame.length) continue;
						if(frame[j].overlaps(playerFutures[frameRef])){
							collideWithEnemy = true;
							break;
						}
					}
					if(collideWithEnemy){
						combinationCost += frameCost;
					}
				}
			}
			 
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
				AIConstant.reactionTime, delta);
		simulatedPlayerPos = rect;
	}
}

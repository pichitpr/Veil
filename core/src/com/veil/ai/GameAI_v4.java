package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.ai.DummyPlayer.PlayerFutureState;
import com.veil.game.element.DynamicEntity;

public class GameAI_v4 extends GameAI {

	//Average frame time = 0.07 sec
	
	//Delay for player to re-decide button press, in this case, AI will not be able to "change" button decision immediately
	private final int buttonChangeDelay = 4;
	private final int simulationFrame = 50;
	private final float yDiffShootingMargin = 0;
	private final int retainDurationAfterRunTowardEnemy = 20;
	
	//If enemy does not take damage within [threshold] frames, try to skip 
	private final int noDamageFrameThreshold = 60*60;
	
	private int buttonChangeDelayCounter = 0;
	private boolean shootLastFrame;
	private boolean runningTowardEnemyState;
	private int runningTowardEnemyStateCounter;
	
	private int lastEnemyHP = -1;
	private int landHitOnEnemyFrame = -1;
	private int accumulatedFrame = 0;
	
	@Override
	protected void onReset() {
		buttonChangeDelayCounter = 0;
		shootLastFrame = false;
		runningTowardEnemyState = false;
		runningTowardEnemyStateCounter = 0;
		
		lastEnemyHP = -1;
		landHitOnEnemyFrame = -1;
		accumulatedFrame = 0;
	}
	
	@Override
	protected void pressButton(Controller controller, LevelSnapshot info,
			float delta) {
		if(lastEnemyHP > 0 && info.enemy != null && info.enemy.getBaseHP() < lastEnemyHP){
			//Enemy hp changed
			lastEnemyHP = info.enemy.getBaseHP();
			landHitOnEnemyFrame = accumulatedFrame;
		}
		
		if(buttonChangeDelayCounter == 0){
			searchButtonCombination(info, delta);
		}else{
			buttonChangeDelayCounter--;
		}
		pressButtonByCombination(controller, info, delta);
		
		//Shooting enemy
		if(!shootLastFrame){
			if(info.enemy != null){
				boolean playerFaceRight = info.player.direction.getX() > 0;
				boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
				Vector2 playerCenter = new Vector2(Vector2.Zero), enemyCenter = new Vector2(Vector2.Zero);
				info.playerRect.getCenter(playerCenter);
				info.enemyRect.getCenter(enemyCenter);
				boolean enemyInShootingRange = Math.abs(playerCenter.y-enemyCenter.y) <=
						(info.enemyRect.height+yDiffShootingMargin)/2f;
				if( ((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)) && enemyInShootingRange){
					controller.shoot = true;
				}
			}
		}
		shootLastFrame = !shootLastFrame;
		
		//Check for skipping if the level is skippable
		if(info.levelTimelimit < 0){
			if(landHitOnEnemyFrame < 0){
				if(accumulatedFrame > noDamageFrameThreshold){
					controller.pause = true;
				}
			}else{
				if(accumulatedFrame - landHitOnEnemyFrame > noDamageFrameThreshold){
					controller.pause = true;
				}
			}
			
		}
		
		accumulatedFrame ++;
	}

	private ButtonCombination currentCombination = ButtonCombination.None;
	
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[][] predictedFrames = new Rectangle[entityTracker.size()][];
		Vector2[] currentDirection = new Vector2[entityTracker.size()];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			predictedFrames[index] = entityTracker.get(dyn).predictNextFrame(simulationFrame);
			currentDirection[index] = entityTracker.get(dyn).getCurrentMovingDirection();
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
			
			PlayerFutureState[] playerFutures = dummy.simulatePosition2(btn.leftPressed(), btn.rightPressed(), false, false, 
					btn.jumpPressed(), simulationFrame, delta);
			for(int frameRef = 0; frameRef<playerFutures.length; frameRef++){
				boolean collideWall = false;
				if(playerFutures[frameRef].hitWall){
					collideWall = true;
				}
				boolean collideEnemy = false;
				for(Rectangle[] frame : predictedFrames){
					if(frame[frameRef].overlaps(playerFutures[frameRef].rect)){
						collideEnemy = true;
						break;
					}
				}
				if(collideEnemy || collideWall){
					combinationCost = (simulationFrame-frameRef)/buttonChangeDelay;
					break;
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

		ButtonCombination newCombination = currentCombination;
		//There are combinations with equal cost
		if(selectedCombination.size() > 1){
			ButtonCombination previousCombination = currentCombination;
			boolean combinationChosen = false;
			if(info.enemy != null){
				//Prioritize turning to enemy and stand still (if safe) when the player is on the floor
				if(info.playerOnFloor && !runningTowardEnemyState){
					boolean playerFaceRight = info.player.direction.getX() > 0;
					boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
					boolean safeToStand = isSafeToStand(info);
					if(safeToStand){
						//Stand still if choice available and already facing enemy
						if((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)){
							if(selectedCombination.contains(ButtonCombination.None)){
								newCombination = ButtonCombination.None;
								combinationChosen = true;
							}
						}
						//Above criteria not satisfied, try to turn towards enemy. If cannot decide, use retain direction strategy
						if(!combinationChosen){
							ButtonCombination comb = getCombinationRunTowardEnemy(selectedCombination, !enemyOnRight, false);
							if(comb != null){
								newCombination = comb;
								combinationChosen = true; 
							}
						}
					}else{
						//It's no safe, avoid enemy now! by either runaway or jump toward
						boolean shouldRunFromEnemy = shouldRunFromEnemy(info);
						ButtonCombination comb = getCombinationRunTowardEnemy(selectedCombination, 
								(shouldRunFromEnemy && enemyOnRight) || (!shouldRunFromEnemy && !enemyOnRight), false);
						if(comb != null){
							newCombination = comb;
							combinationChosen = true;
							if(!shouldRunFromEnemy){
								runningTowardEnemyState = true;
								runningTowardEnemyStateCounter = retainDurationAfterRunTowardEnemy;
							}
						}
					}
				}
			}
			if(runningTowardEnemyState){
				runningTowardEnemyStateCounter--;
				if(runningTowardEnemyStateCounter == 0){
					runningTowardEnemyState = false;
				}
			}
			//Retain previous direction strategy
			if(!combinationChosen){
				//Try to retain moving direction when jumping
				List<ButtonCombination> retained = new ArrayList<ButtonCombination>();
				for(ButtonCombination comb : selectedCombination){
					if(startFallingDown){
						//Exclude jumping choice during fall down but direction must be retained
						if(comb.sameMovingDirectionAs(previousCombination)){
							retained.add(comb.nonJumpVersion());
						}
					}else{
						//Prioritize previous combination
						if(comb == previousCombination){
							return;
						}
						//Check if the combination can retain moving direction (left-right only)
						if(comb.sameMovingDirectionAs(previousCombination)){
							retained.add(comb);
						}
					}
				}
				if(retained.size() > 0){
					newCombination = retained.get( MathUtils.random(0,retained.size()-1) );
				}else{
					newCombination = selectedCombination.get( MathUtils.random(0,selectedCombination.size()-1) );
				}
			}
		}else{
			newCombination = selectedCombination.get(0);
		}
		
		//Decision change, must retain combination for reactionTime
		if(newCombination != currentCombination){
			buttonChangeDelayCounter = buttonChangeDelay;
		}
		currentCombination = newCombination;
	}
	
	private boolean isSafeToStand(LevelSnapshot info){
		return (Math.abs(info.playerRect.x - info.enemyRect.x) - (info.playerRect.width+info.enemyRect.width)/2) >= 240;
	}
	
	private boolean shouldRunFromEnemy(LevelSnapshot info){
		return (Math.abs(info.playerRect.x - info.enemyRect.x) - (info.playerRect.width+info.enemyRect.width)/2) >= 180;
	}
	
	private ButtonCombination getCombinationRunTowardEnemy(List<ButtonCombination> combs, boolean left, 
			boolean prioritizeJump){
		ButtonCombination result = null;
		for(ButtonCombination comb : combs){
			if((comb.leftPressed() && left) || (comb.rightPressed() && !left)){
				if(result == null || (comb.jumpPressed() && prioritizeJump) || (!comb.jumpPressed() && !prioritizeJump) ){
					result = comb;
				}
			}
		}
		return result;
	}
	
	private void pressButtonByCombination(Controller controller, LevelSnapshot info, float delta){
		controller.left = currentCombination.leftPressed();
		controller.right = currentCombination.rightPressed();
		//Require jumping, check if jumping should be delayed by 1 frame or not (release jumping button before pressing again)
		if(currentCombination.jumpPressed()){
			controller.jump = !startFallingDown;
		}
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		dummy.mimicPlayer(info.player);
		Rectangle[] rect = dummy.simulatePosition(controller.left, controller.right, controller.up, controller.down, controller.jump,
				buttonChangeDelay, delta);
		simulatedPlayerPos = rect;
	}
}

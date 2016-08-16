package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.ai.DummyPlayer.PlayerFutureState;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;

public class GameAI_v4 extends GameAI {

	//Average frame time = 0.07 sec
	
	//Delay for player to re-decide button press, in this case, AI will not be able to "change" button decision immediately
	private final int buttonChangeDelay = 4;
	private final int simulationFrame = 50;
	private final float yDiffShootingMargin = 8;
	private final int retainDurationAfterRunTowardEnemy = 40;
	
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
		
		//Shooting enemy or minion
		if(!shootLastFrame){
			controller.shoot = shouldShootThisEntity(info, info.enemy, true);
			if(!controller.shoot){
				for(DynamicEntity dyn : info.tempRect.keySet()){
					if(shouldShootThisEntity(info, dyn, false)){
						controller.shoot = true;
						break;
					}
				}
			}
			if(controller.shoot){
				shootLastFrame = true;
			}
		}else{
			shootLastFrame = false;
		}
		
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
		//Loop through all possible button presses and pick the safest route (prioritize safety)
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
		if(selectedCombination.size() == 1){
			newCombination = selectedCombination.get(0);
		}else{
			/*
			 * There are button combinations with equal cost (all combinations are considered safest route by AI)
			 * If enemy exists, prioritize: 
			 * 		1) No horizontal movement + facing enemy + keep same Y as enemy (Attacking phase)
			 * 		2) Facing enemy + allow horizontal movement to keep same Y as enemy
			 * 		3) Avoid + keep same Y as enemy
			 * 		4) Avoid
			 * If cannot decide, try to retain moving direction
			 */
			ButtonCombination previousCombination = currentCombination;
			boolean combinationChosen = false;
			if(info.enemy != null){
				//Prioritize turning to enemy and stand still (if safe) when the player is on the floor
				if(info.playerOnFloor && !runningTowardEnemyState){
					boolean playerFaceRight = info.player.direction.getX() > 0;
					boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
					boolean safeToStand = isSafeToStand(info);
					if(safeToStand){
						//Already facing enemy
						if((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)){
							if(isEnemyAbove(info)){
								//Try to keep the same Y as enemy by jumping if possible -- moving is allow if needed
								if(selectedCombination.contains(ButtonCombination.Jump)){
									newCombination = ButtonCombination.Jump;
									combinationChosen = true;
								}else{
									if(playerFaceRight){
										if(selectedCombination.contains(ButtonCombination.RightJump)){
											newCombination = ButtonCombination.RightJump;
											combinationChosen = true;
										}
									}else{
										if(selectedCombination.contains(ButtonCombination.LeftJump)){
											newCombination = ButtonCombination.LeftJump;
											combinationChosen = true;
										}
									}
								}
							}else{
								//Stand still if choice is available
								if(selectedCombination.contains(ButtonCombination.None)){
									newCombination = ButtonCombination.None;
									combinationChosen = true;
								}
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
						//It's not safe, avoid enemy now! by either runaway or jump toward
						boolean prioritizeLeft = false;
						if(isLargeEnemy(info)){
							float leftGap = getEnemyGap(info, true);
							float rightGap = getEnemyGap(info, false);
							prioritizeLeft = leftGap > rightGap;
						}else{
							boolean shouldRunFromEnemy = shouldRunFromEnemy(info);
							prioritizeLeft = (shouldRunFromEnemy && enemyOnRight) || (!shouldRunFromEnemy && !enemyOnRight);
						}
						ButtonCombination comb = getCombinationRunTowardEnemy(selectedCombination, prioritizeLeft, 
								isEnemyAbove(info));
						if(comb != null){
							newCombination = comb;
							combinationChosen = true;
							runningTowardEnemyState = true;
							runningTowardEnemyStateCounter = retainDurationAfterRunTowardEnemy;
							/*
							if(!shouldRunFromEnemy){
								runningTowardEnemyState = true;
								runningTowardEnemyStateCounter = retainDurationAfterRunTowardEnemy;
							}
							*/
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
		}
		
		//Decision change, must retain combination for reactionTime
		if(newCombination != currentCombination){
			buttonChangeDelayCounter = buttonChangeDelay;
		}
		currentCombination = newCombination;
	}
	
	private boolean isLargeEnemy(LevelSnapshot info){
		return info.enemyRect.width >= 128 || info.enemyRect.height >= 128;
	}
	
	private float getEnemyGap(LevelSnapshot info, boolean left){
		return left ? info.enemyRect.x - GameConstant.tileSizeX*2 : 
			info.level.getStaticMap().getMapSize()[0] - GameConstant.tileSizeX - info.playerRect.width - 
			(info.enemyRect.x + info.enemyRect.width);
	}
	
	/**
	 * Return true (considered safe) if enemy is more than 240 pixels away
	 */
	private boolean isSafeToStand(LevelSnapshot info){
		float dsp = Math.abs(info.playerRect.x - info.enemyRect.x) - (info.playerRect.width+info.enemyRect.width)/2;
		float minSafeDsp =  (info.level.getStaticMap().getMapSize()[0] - GameConstant.tileSizeX*2 - info.enemyRect.width)*0.4f;
		/*
		float availableSafeDsp = info.playerRect.x < info.enemyRect.x ?  info.enemyRect.x - GameConstant.tileSizeX*2 : 
			info.level.getStaticMap().getMapSize()[0] - GameConstant.tileSizeX - info.playerRect.width - (info.enemyRect.x + info.enemyRect.width);
			*/ 
		if(isLargeEnemy(info)){
			return dsp >= minSafeDsp;
		}else{
			return dsp >= minSafeDsp;
		}
	}
	
	/**
	 * In avoiding phase, player should runaway if it is more than 180 pixels away from enemy
	 */
	private boolean shouldRunFromEnemy(LevelSnapshot info){
		return (Math.abs(info.playerRect.x - info.enemyRect.x) - (info.playerRect.width+info.enemyRect.width)/2) >= 120;//180;
	}
	
	/**
	 * Return combination that causes player to run towards specified direction (run left if left == true)
	 * If prioritizeJump is true, this method pick combination with jump button pressed
	 */
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
	
	private boolean isEnemyAbove(LevelSnapshot info){
		return info.enemyRect.y - info.playerRect.y > info.enemyRect.height/2 ;
	}
	
	private boolean shouldShootThisEntity(LevelSnapshot info, DynamicEntity dyn, boolean isEnemy){
		if(dyn == null) return false;;
		if(!isEnemy && (dyn.invul || !dyn.defender)) return false;
		Vector2 playerCenter = new Vector2(Vector2.Zero), enemyCenter = new Vector2(Vector2.Zero);
		info.playerRect.getCenter(playerCenter);
		dyn.getWorldCollider().getCenter(enemyCenter);
		boolean enemyInShootingRange = Math.abs(playerCenter.y-enemyCenter.y) <=
				(dyn.getWorldCollider().height+yDiffShootingMargin)/2f;
		if(Math.abs(playerCenter.x-enemyCenter.x) <= dyn.getWorldCollider().width/2f && enemyInShootingRange){
			return true;
		}
		boolean playerFaceRight = info.player.direction.getX() > 0;
		boolean enemyOnRight = info.playerRect.x < dyn.getWorldCollider().x;
		if( ((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)) && enemyInShootingRange){
			return true;
		}
		return false;
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

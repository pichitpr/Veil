package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.ai.DummyPlayer.PlayerFutureState;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;

public class GameAI_v5 extends GameAI {

	public int buttonChangeDelay = 4;
	public int simulationFrame = 50;
	public float yDiffShootingMargin = 8;
	public int shootingDelay = 0;
	public int retainDurationAfterRunTowardEnemy = 40;
	
	//If enemy does not take damage within [threshold] frames, try to skip 
	private final int noDamageFrameThreshold = 60*60;
	
	private int buttonChangeDelayCounter = 0;
	private boolean runningTowardEnemyState;
	private int runningTowardEnemyStateCounter;
	
	private int shootDelayCounter;
	private int lastEnemyHP = -1;
	private int landHitOnEnemyFrame = -1;
	private int accumulatedFrame = 0;
	
	private ButtonCombination currentCombination;
	
	@Override
	protected void onReset(DynamicEntity initialStateEnemy) {
		buttonChangeDelayCounter = 0;
		runningTowardEnemyState = false;
		runningTowardEnemyStateCounter = 0;
		
		shootDelayCounter = 0;
		lastEnemyHP = initialStateEnemy.getBaseHP();
		landHitOnEnemyFrame = -1;
		accumulatedFrame = 0;
		
		currentCombination = ButtonCombination.None;
	}

	@Override
	protected void pressButton(Controller controller, LevelSnapshot info,
			float delta) {
		if(lastEnemyHP > 0 && info.enemy != null && info.enemy.getBaseHP() < lastEnemyHP){
			//Enemy hp changed
			lastEnemyHP = info.enemy.getBaseHP();
			landHitOnEnemyFrame = accumulatedFrame;
		}
		
		//Handle button press decision for fighting
		aiPressButton(controller, info, delta);
		
		//Check for skipping if the level is skippable
		if(info.levelTimelimit < -10){
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

	private void aiPressButton(Controller controller, LevelSnapshot info, float delta){
		if(buttonChangeDelayCounter == 0){
			searchButtonCombination(info, delta);
		}else{
			buttonChangeDelayCounter--;
		}
		controller.left = currentCombination.leftPressed();
		controller.right = currentCombination.rightPressed();
		//Require jumping, check if jumping should be delayed by 1 frame or not (release jumping button before pressing again)
		if(currentCombination.jumpPressed()){
			controller.jump = !startFallingDown;
		}
		if(shootDelayCounter <= 0){
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
				shootDelayCounter = shootingDelay+1; //Add 1 for release button frame
			}
		}else{
			shootDelayCounter--;
		}
	}
	
	/**
	 * Search for best direction control and put result in currentCombination
	 */
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[] predictedMainEnemy = null;
		//EntityTracker may not include enemy if enemy is null (already removed from game world)
		Rectangle[][] predictedEnemies = new Rectangle[entityTracker.size()-(info.enemy == null ? 0 : 1)][];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			if(dyn == info.enemy){
				predictedMainEnemy = entityTracker.get(dyn).predictNextFrame(simulationFrame);
			}else{
				predictedEnemies[index] = entityTracker.get(dyn).predictNextFrame(simulationFrame);
				index++;
			}
		}
		
		//Search for best combination		
		int minCost = Integer.MAX_VALUE;
		List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
		//Loop through all possible button presses and pick the best
		for(ButtonCombination btn : ButtonCombination.values()){
			int cost = calculateCombinationCost(btn, info.enemy, predictedMainEnemy, predictedEnemies, info, delta);
			if(cost <= minCost){
				if(cost < minCost){
					selectedCombination.clear();
					minCost = cost;
				}
				selectedCombination.add(btn);
			}
		}
		
		ButtonCombination newCombination = currentCombination;
		if(selectedCombination.size() == 1){
			newCombination = selectedCombination.get(0);
		}else{
			/*
			 * There are button combinations with equal cost
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
						boolean shouldRunFromEnemy = false;
						if(isLargeEnemy(info)){
							float leftGap = getEnemyGap(info, true);
							float rightGap = getEnemyGap(info, false);
							prioritizeLeft = leftGap > rightGap;
							shouldRunFromEnemy = true;
						}else{
							shouldRunFromEnemy = shouldRunFromEnemy(info);
							prioritizeLeft = (shouldRunFromEnemy && enemyOnRight) || (!shouldRunFromEnemy && !enemyOnRight);
						}
						ButtonCombination comb = getCombinationRunTowardEnemy(selectedCombination, prioritizeLeft, 
								isEnemyAbove(info));
						if(comb != null){
							newCombination = comb;
							combinationChosen = true;
							/*
							runningTowardEnemyState = true;
							runningTowardEnemyStateCounter = retainDurationAfterRunTowardEnemy;
							*/
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
		}
		
		//Decision change, must retain combination for reactionTime
		if(newCombination != currentCombination){
			buttonChangeDelayCounter = buttonChangeDelay;
		}
		currentCombination = newCombination;
	}
	
	/**
	 * Get a cost of pressing specified button combination with current game state. The lower the better, cost can be negative.
	 * Cost is calculated by checking for possible future event when the button remains pressed.
	 */
	private int calculateCombinationCost(ButtonCombination inspecting, DynamicEntity mainEnemy, Rectangle[] predictedMainEnemy, 
			Rectangle[][] predictedEnemies, LevelSnapshot info, float delta){
		int cost = 0;
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		dummy.mimicPlayer(info.player);
		PlayerFutureState[] playerFutures = dummy.simulatePosition2(inspecting.leftPressed(), inspecting.rightPressed(), false, false, 
				inspecting.jumpPressed(), simulationFrame, delta);
		boolean firstCollideWallCalculated = false;
		boolean firstCollisionCalculated = false;
		int shootableFrame = 0;
		for(int frameRef = 0; frameRef<playerFutures.length; frameRef++){
			boolean collideWall = false;
			if(playerFutures[frameRef].hitWall){
				collideWall = true;
			}
			int collideEnemyCount = 0;
			if(predictedMainEnemy != null){
				if(predictedMainEnemy[frameRef].overlaps(playerFutures[frameRef].rect)){
					collideEnemyCount++;
				}
			}
			for(Rectangle[] frame : predictedEnemies){
				if(frame[frameRef].overlaps(playerFutures[frameRef].rect)){
					collideEnemyCount++;
					break;
				}
			}
			
			if(!firstCollideWallCalculated && collideWall){
				//Avoid wall if it is in the near future
				if(frameRef < 10)
					cost += 100;
				firstCollideWallCalculated = true;
			}
			if(!firstCollisionCalculated && collideEnemyCount > 0){
				//Cost increased based on frame number, the sooner the higher cost.
				int collisionCost = simulationFrame-frameRef;
				if(frameRef >= 30){
					//Do not care if enemy collision happen in far future (frame 30 onward)
					collisionCost = 0;
				}
				cost += collisionCost;
				firstCollisionCalculated = true;
			}
			
			//If player has high enough HP, AI will consider shooting
			if(info.player.getBaseHP() > 5 && frameRef >= shootableFrame){
				if(mainEnemy != null && shouldShootThisEntity(playerFutures[frameRef].rect, playerFutures[frameRef].facingRight, 
						predictedMainEnemy[frameRef], mainEnemy.invul, mainEnemy.defender, true)){
					cost--;
					shootableFrame = frameRef + shootingDelay + 2;
				}
			}
		}
		
		return cost;
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
		if(isLargeEnemy(info)){
			return dsp >= 240;
		}else{
			return dsp >= 240;
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
		if(dyn == null) return false;
		return shouldShootThisEntity(info.playerRect, info.player.direction.getX() > 0, dyn.getWorldCollider(), dyn.invul, dyn.defender,
				isEnemy);
	}
	
	private boolean shouldShootThisEntity(Rectangle playerRect, boolean playerFaceRight, Rectangle dynRect, 
			boolean dynInvul, boolean dynDefender, boolean isEnemy){
		if(!isEnemy && (dynInvul || !dynDefender)) return false;
		Vector2 playerCenter = new Vector2(Vector2.Zero), enemyCenter = new Vector2(Vector2.Zero);
		playerRect.getCenter(playerCenter);
		dynRect.getCenter(enemyCenter);
		boolean enemyInShootingRange = Math.abs(playerCenter.y-enemyCenter.y) <=
				(dynRect.height+yDiffShootingMargin)/2f;
		if(Math.abs(playerCenter.x-enemyCenter.x) <= dynRect.width/2f && enemyInShootingRange){
			return true;
		}
		boolean enemyOnRight = playerRect.x < dynRect.x;
		if( ((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)) && enemyInShootingRange){
			return true;
		}
		return false;
	}
}

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
	
	//If enemy is in "undamagable" state for [threshold] frames, try to wander
	private final int invulFrameThreshold = 60*3;
	
	//If enemy is out of reach for [threshold] frames, try to wonder
	private final int outOfReachThreshold = 60*3;
	
	//If enemy does not take damage within [threshold] frames, AI may consider wandering
	private final int noDamageFrameThresholdForTransition = 60*4;
	
	//If any attack come closer than [threshold], stop wandering
	private final float threatProximityThreshold = 180;
	
	//A number of frame to ignore enemy collision when searching future (prevent biased cost when player already collides with enemy)
	private final int ignoreEnemyFrameCount = 10;
	
	private int buttonChangeDelayCounter = 0;
	private int stateChangeDelayCounter = 0;
	private boolean isFightingState;
	
	//Wandering state variable
	private boolean wanderLeft;
	
	//Fighting state variable
	private boolean runningTowardEnemyState;
	private int runningTowardEnemyStateCounter;
	
	private int shootDelayCounter;
	//Enemy additional info
	private int lastEnemyHP = -1;
	private int landHitOnEnemyFrame = -1;
	private int accumulatedFrame = 0;
	private int invulCounter;
	private int outOfReachCounter;
	
	private ButtonCombination currentCombination;
	
	@Override
	protected void onReset(DynamicEntity initialStateEnemy) {
		buttonChangeDelayCounter = 0;
		isFightingState = true;
		resetWander();
		resetFight();
		
		shootDelayCounter = 0;
		lastEnemyHP = initialStateEnemy.getBaseHP();
		landHitOnEnemyFrame = -1;
		accumulatedFrame = 0;
		invulCounter = 0;
		outOfReachCounter = 0;
		
		currentCombination = ButtonCombination.None;
	}

	@Override
	protected void pressButton(Controller controller, LevelSnapshot info,
			float delta) {
		//Update enemy additional info
		if(info.enemy != null){
			if(lastEnemyHP > 0 && info.enemy.getBaseHP() < lastEnemyHP){
				//Enemy hp changed
				lastEnemyHP = info.enemy.getBaseHP();
				landHitOnEnemyFrame = accumulatedFrame;
			}
			if(!info.enemy.invul && info.enemy.defender){
				invulCounter = 0;
			}else{
				invulCounter++;
			}
			if(enemyIsInShootingRange(info.playerRect, info.enemyRect)){
				outOfReachCounter = 0;
			}else{
				outOfReachCounter++;
			}
		}else{
			invulCounter = 0;
			outOfReachCounter = 0;
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
		//Press button
		stateChangeDelayCounter--;
		if(buttonChangeDelayCounter == 0){
			//Delay is to prevent AI to toggle from fighting state to wandering state too fast
			//However, toggle from wandering to fighting immediately is required to avoid attack
			if(isFightingState){
				searchButtonCombination(info, delta);
				if(shouldEndFight(info, delta) && stateChangeDelayCounter <= 0){
					resetFight();
					isFightingState = false;
				}
			}else{
				searchButtonCombinationWander(info, delta);
				if(shouldEndWander(info, delta)){
					resetWander();
					isFightingState = true;
					stateChangeDelayCounter = 30;
				}
			}
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
		
		DummyPlayer dummy = new DummyPlayer(info.level, 1);
		dummy.mimicPlayer(info.player);
		Rectangle[] rect = dummy.simulatePosition(controller.left, controller.right, controller.up, controller.down, controller.jump,
				buttonChangeDelay, delta);
		simulatedPlayerPos = rect;
	}
	
	private void resetWander(){
		wanderLeft = false;
	}
	
	private boolean shouldEndWander(LevelSnapshot info, float delta){
		//No enemy, end wandering state immediately
		if(info.enemy == null)
			return true;
		
		//Enemy's attack in proximity, end wandering state immediately
		for(Rectangle dynRect : info.tempRect.values()){
			if(dynRect.overlaps(info.playerRect)){
				return true;
			}else{
				boolean xDstInProximity = xCenterDst(info.playerRect, dynRect) <=
						(info.playerRect.width+dynRect.width)/2f + threatProximityThreshold;
				boolean yDstInProximity = yCenterDst(info.playerRect, dynRect) <=
						(info.playerRect.height+dynRect.height)/2f + threatProximityThreshold;
				if(xDstInProximity && yDstInProximity){
					return true;
				}
			}
		}
		
		//Visible enemy and it is either damagable now OR AI might collide with enemy --> end wandering
		Rectangle screenRect = new Rectangle(0, 0, GameConstant.screenW, GameConstant.screenH);
		if(screenRect.overlaps(info.enemyRect)){
			//Now damagable
			if(info.levelTimelimit < -10 && invulCounter < invulFrameThreshold && outOfReachCounter < outOfReachThreshold){
				return true;
			}
			//Not damagable but too close
			if(xCenterDst(info.playerRect, info.enemyRect) < (info.playerRect.width+info.enemyRect.width)/2f+GameConstant.tileSizeX*2 && 
					yCenterDst(info.playerRect, info.enemyRect) < (info.playerRect.height+info.enemyRect.height)/2f+GameConstant.tileSizeY*2){
				return true;
			}
		}

		//Otherwise, continue
		return false;
	}
	
	private void searchButtonCombinationWander(LevelSnapshot info, float delta){
		if(info.playerState.surfaceInFront[1]){
			//Wall on east
			wanderLeft = true;
		}else if(info.playerState.surfaceInFront[3]){
			//Wall on west
			wanderLeft = false;
		}
		if(wanderLeft){
			currentCombination = ButtonCombination.LeftJump;
		}else{
			currentCombination = ButtonCombination.RightJump;
		}
	}
	
	private void resetFight(){
		runningTowardEnemyState = false;
		runningTowardEnemyStateCounter = 0;
	}
	
	private boolean shouldEndFight(LevelSnapshot info, float delta){
		//If player still manage to damage enemy, continue fighting
		if(info.enemy == null || accumulatedFrame < noDamageFrameThresholdForTransition){
			return false;
		}
		
		//Player cannot damage enemy for sometime
		Rectangle screenRect = new Rectangle(0, 0, GameConstant.screenW, GameConstant.screenH);
		if(!screenRect.overlaps(info.enemyRect)){
			//Enemy is not visible, try to wander
			return true;
		}else{
			//Enemy is visible but stay invul/no defender/out of reach for too long without being "unbeatable", try to wander instead
			if(info.levelTimelimit < -10 && (invulCounter > invulFrameThreshold || outOfReachCounter > outOfReachThreshold)){
				return true;
			}
			//Otherwise, continue fighting
			return false;
		}
	}
	
	/**
	 * Search for best direction control and put result in currentCombination
	 */
	private void searchButtonCombination(LevelSnapshot info, float delta){
		Rectangle[] predictedMainEnemy = null;
		//EntityTracker may not include enemy if enemy is null (already removed from game world)
		Rectangle[][] predictedEnemies = new Rectangle[entityTracker.size()-(info.enemy == null ? 0 : 1)][];
		Rectangle[] currentEnemies = new Rectangle[predictedEnemies.length];
		int index=0;
		for(DynamicEntity dyn : entityTracker.keySet()){
			if(dyn == info.enemy){
				predictedMainEnemy = entityTracker.get(dyn).predictNextFrame(simulationFrame);
			}else{
				predictedEnemies[index] = entityTracker.get(dyn).predictNextFrame(simulationFrame);
				currentEnemies[index] = dyn.getWorldCollider();
				index++;
			}
		}
		
		Rectangle[][] predicted = new Rectangle[predictedEnemies.length+(info.enemy == null ? 0 : 1)][];
		for(int i=0; i<predictedEnemies.length;i++){
			predicted[i] = predictedEnemies[i];
		}
		if(info.enemy != null){
			predicted[predicted.length-1] = predictedMainEnemy;
		}
		predictedEnemyPos = predicted;
		
		//Search for best combination		
		int minCost = Integer.MAX_VALUE;
		List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
		//Loop through all possible button presses and pick the best
		for(ButtonCombination btn : ButtonCombination.values()){
			int cost = calculateCombinationCost(btn, info.enemy, currentEnemies, predictedMainEnemy, predictedEnemies, info, delta);
			//System.out.print(btn+":"+cost+"  ");
			if(cost <= minCost){
				if(cost < minCost){
					selectedCombination.clear();
					minCost = cost;
				}
				selectedCombination.add(btn);
			}
		}
		//System.out.println("");
		
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
					boolean safeToStand = isSafeToStand(info.playerRect, info.enemyRect);
					if(safeToStand){
						//Already facing enemy
						if((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)){
							if(isEnemyAbove(info.playerRect, info.enemyRect)){
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
						if(isLargeEnemy(info.enemyRect)){
							float leftGap = getEnemyGap(info, true);
							float rightGap = getEnemyGap(info, false);
							prioritizeLeft = leftGap > rightGap;
							shouldRunFromEnemy = true;
						}else{
							shouldRunFromEnemy = shouldRunFromEnemy(info.playerRect, info.enemyRect);
							prioritizeLeft = (shouldRunFromEnemy && enemyOnRight) || (!shouldRunFromEnemy && !enemyOnRight);
						}
						ButtonCombination comb = getCombinationRunTowardEnemy(selectedCombination, prioritizeLeft, 
								isEnemyAbove(info.playerRect, info.enemyRect));
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
	private int calculateCombinationCost(ButtonCombination inspecting, DynamicEntity mainEnemy, Rectangle[] currentEnemies, 
			Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, LevelSnapshot info, float delta){
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
				//If player overlaps with enemy, ignore a few first future frames
				if(!info.playerRect.overlaps(mainEnemy.getWorldCollider()) || frameRef >= ignoreEnemyFrameCount){
					if(predictedMainEnemy[frameRef].overlaps(playerFutures[frameRef].rect)){
						collideEnemyCount++;
					}
				}
			}
			for(int i=0; i<predictedEnemies.length; i++){
				//If player overlaps with enemy, ignore a few first future frames
				if(!currentEnemies[i].overlaps(info.playerRect) || frameRef >= ignoreEnemyFrameCount){
					if(predictedEnemies[i][frameRef].overlaps(playerFutures[frameRef].rect)){
						collideEnemyCount++;
						break;
					}
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
	
	private boolean isLargeEnemy(Rectangle enemyRect){
		return enemyRect.width >= 128 || enemyRect.height >= 128;
	}
	
	/**
	 * Return empty space on either side of the enemy
	 */
	private float getEnemyGap(LevelSnapshot info, boolean left){
		return left ? info.enemyRect.x - GameConstant.tileSizeX : 
			info.level.getStaticMap().getMapSize()[0] - GameConstant.tileSizeX - (info.enemyRect.x + info.enemyRect.width);
	}
	
	/**
	 * Return true (considered safe) if enemy is more than 240 pixels away
	 */
	private boolean isSafeToStand(Rectangle playerRect, Rectangle enemyRect){
		float dsp = xCenterDst(playerRect, enemyRect) - (playerRect.width + enemyRect.width)/2;
		if(isLargeEnemy(enemyRect)){
			return dsp >= 240;
		}else{
			return dsp >= 240;
		}
	}
	
	/**
	 * In avoiding phase, player should runaway if it is more than 120 pixels away from enemy.
	 * Otherwise, try running toward enemy and jump over instead
	 */
	private boolean shouldRunFromEnemy(Rectangle playerRect, Rectangle enemyRect){
		return (xCenterDst(playerRect, enemyRect) - (playerRect.width + enemyRect.width)/2) >= 120;
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
	
	private boolean isEnemyAbove(Rectangle playerRect, Rectangle enemyRect){
		return yCenterDiff(enemyRect, playerRect) > enemyRect.height/2 ;
	}
	
	private boolean shouldShootThisEntity(LevelSnapshot info, DynamicEntity dyn, boolean isEnemy){
		if(dyn == null) return false;
		return shouldShootThisEntity(info.playerRect, info.player.direction.getX() > 0, dyn.getWorldCollider(), dyn.invul, dyn.defender,
				isEnemy);
	}
	
	private boolean shouldShootThisEntity(Rectangle playerRect, boolean playerFaceRight, Rectangle dynRect, 
			boolean dynInvul, boolean dynDefender, boolean isEnemy){
		if(!isEnemy && (dynInvul || !dynDefender)) return false;
		boolean xOverlap = xCenterDst(playerRect,dynRect) < dynRect.width/2f;
		boolean enemyInShootingRange = enemyIsInShootingRange(playerRect, dynRect);
		if(xOverlap && enemyInShootingRange){
			return true;
		}
		boolean enemyOnRight = playerRect.x < dynRect.x;
		if( ((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)) && enemyInShootingRange){
			return true;
		}
		return false;
	}
	
	private boolean enemyIsInShootingRange(Rectangle playerRect, Rectangle dynRect){
		return yCenterDst(playerRect, dynRect) <= (dynRect.height+yDiffShootingMargin)/2f;
	}
	
	//========================== Utility
	
	private float xCenterDst(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return Math.abs(rect1.getCenter(v1).x - rect2.getCenter(v2).x);
	}
	
	private float yCenterDst(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return Math.abs(rect1.getCenter(v1).y - rect2.getCenter(v2).y);
	}
	
	private float yCenterDiff(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return rect1.getCenter(v1).y - rect2.getCenter(v2).y;
	}
}

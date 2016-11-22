package com.veil.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.ai.DummyPlayer.PlayerFutureState;
import com.veil.ai.GameAI.ButtonCombination;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;

public class GameAI_v6 extends GameAI {

	public static int buttonChangeDelay = 15;
	public static int simulationFrame = 50;
	public static float yDiffShootingMargin = 8;
	public static int shootingDelay = 0;
	
	public static int noDamageFrameThreshold = 60*30;
	
	public static int noDamageFrameThresholdForTransition = 60*4;
	
	public static int ignoreEnemyFrameCount = 10;
	
	public static int ignoreWallAfterFrameRef = 10;
	public static int ignoreEnemyAfterFrameRef = 30;
	
	public static float threatProximityThreshold = 180;
	public static int invulFrameThreshold = 60*3;
	public static int outOfReachThreshold = 60*3;
	
	
	public class ExtraInfo {
		public int lastEnemyHP = -1;
		public int landHitOnEnemyFrame = -1;
		public int accumulatedFrame = 0;
		public int invulCounter;
		public int outOfReachCounter;
		
		public void reset(DynamicEntity initialStateEnemy){
			lastEnemyHP = initialStateEnemy.getBaseHP();
			landHitOnEnemyFrame = -1;
			accumulatedFrame = -1;
			invulCounter = 0;
			outOfReachCounter = 0;
		}
		
		public void update(LevelSnapshot info){
			accumulatedFrame++;
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
				if(GameAI_v6.enemyIsInShootingRange(info.playerRect, info.enemyRect)){
					outOfReachCounter = 0;
				}else{
					outOfReachCounter++;
				}
			}else{
				invulCounter = 0;
				outOfReachCounter = 0;
			}
		}
	}
	
	public interface AITask {
		public AITask chooseTask(LevelSnapshot info, ExtraInfo xInfo, 
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, float delta);
		public ButtonCombination chooseCombination(ButtonCombination prevBtn, LevelSnapshot info, ExtraInfo xInfo, 
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, float delta);
	}
	
	public class AIFightTask implements AITask {

		@Override
		public AITask chooseTask(LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, float delta) {
			if(info.enemy == null){
				return this;
			}
			
			if(xInfo.accumulatedFrame > noDamageFrameThresholdForTransition){
				Rectangle screenRect = new Rectangle(0, 0, GameConstant.screenW, GameConstant.screenH);
				if(!screenRect.overlaps(info.enemyRect)){
					//Enemy is not visible, try to wander
					return wanderTask;
				}else{
					//Enemy is visible but stay invul/no defender/out of reach for too long without being "unbeatable", try to wander instead
					if(info.levelTimelimit < -10 && (xInfo.invulCounter > invulFrameThreshold || xInfo.outOfReachCounter > outOfReachThreshold)){
						return wanderTask;
					}
				}
			}else{
				if(xCenterDst(info.playerRect, info.enemyRect) < 160){
					return keepDistanceTask;
				}
			}
			return this;
		}

		@Override
		public ButtonCombination chooseCombination(ButtonCombination prevBtn, LevelSnapshot info, ExtraInfo xInfo, 
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, float delta) {
			//Search for best combination
			//System.out.print("Fight  ");
			int minCost = Integer.MAX_VALUE;
			List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
			//Loop through all possible button presses and pick the best
			for(ButtonCombination btn : ButtonCombination.values()){
				//System.out.print(btn+":(");
				int cost = calculateCombinationCost(btn, info.enemy, currentEnemies, predictedMainEnemy, predictedEnemies, info, delta);
				//System.out.print(")="+cost+"  ");
				if(cost <= minCost){
					if(cost < minCost){
						selectedCombination.clear();
						minCost = cost;
					}
					selectedCombination.add(btn);
				}
			}
			//System.out.println("");
			
			ButtonCombination newCombination = prevBtn;
			if(selectedCombination.size() == 1){
				newCombination = selectedCombination.get(0);
			}else{
				//More than 1 combination with equals cost
				//Priority: try to face enemy and shoot > retain moving direction > random
				boolean combinationChosen = false;
				
				
				if(info.enemy != null){
					//Prioritize turning to enemy and stand still (if safe) when the player is on the floor
					boolean playerFaceRight = info.player.direction.getX() > 0;
					boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
					if((enemyOnRight && playerFaceRight) || (!enemyOnRight && !playerFaceRight)){
						//Already facing toward enemy, attack!
						if(GameAI_v6.isEnemyAbove(info.playerRect, info.enemyRect)){
							//Try to keep the same Y as enemy by jumping if possible
							//Priority: jump > moving+jump
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
							//Priority: stand > move
							if(selectedCombination.contains(ButtonCombination.None)){
								newCombination = ButtonCombination.None;
								combinationChosen = true;
							}else{
								if(playerFaceRight){
									if(selectedCombination.contains(ButtonCombination.Right)){
										newCombination = ButtonCombination.Right;
										combinationChosen = true;
									}
								}else{
									if(selectedCombination.contains(ButtonCombination.Left)){
										newCombination = ButtonCombination.Left;
										combinationChosen = true;
									}
								}
							}
						}
					}else{
						//Not facing enemy, turn toward it
						if(enemyOnRight){
							if(selectedCombination.contains(ButtonCombination.RightJump)){
								newCombination = ButtonCombination.RightJump;
								combinationChosen = true;
							}else if(selectedCombination.contains(ButtonCombination.Right)){
								newCombination = ButtonCombination.Right;
								combinationChosen = true;
							}
						}else{
							if(selectedCombination.contains(ButtonCombination.LeftJump)){
								newCombination = ButtonCombination.LeftJump;
								combinationChosen = true;
							}else if(selectedCombination.contains(ButtonCombination.Left)){
								newCombination = ButtonCombination.Left;
								combinationChosen = true;
							}
						}
					}
				}
				
				//Retain direction
				if(!combinationChosen){
					//Try to retain moving direction when jumping
					List<ButtonCombination> retained = new ArrayList<ButtonCombination>();
					for(ButtonCombination comb : selectedCombination){
						if(startFallingDown){
							//Exclude jumping choice during fall down but direction must be retained
							if(comb.sameMovingDirectionAs(prevBtn)){
								retained.add(comb.nonJumpVersion());
							}
						}else{
							//Prioritize previous combination
							if(comb == prevBtn){
								newCombination = comb;
								combinationChosen = true;
								break;
							}
							//Check if the combination can retain moving direction (left-right only)
							if(comb.sameMovingDirectionAs(prevBtn)){
								retained.add(comb);
							}
						}
					}
					if(!combinationChosen && retained.size() > 0){
						newCombination = retained.get( MathUtils.random(0,retained.size()-1) );
						combinationChosen = true;
					}
				}
				
				//Random
				if(!combinationChosen){
					newCombination = selectedCombination.get( MathUtils.random(0,selectedCombination.size()-1) );
				}
			}
			
			return newCombination;
		}
		
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
			int costReduced = 0;
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
					if(frameRef < ignoreWallAfterFrameRef){
						cost += 100;
						//System.out.print("100 ");
					}
					firstCollideWallCalculated = true;
				}
				if(!firstCollisionCalculated && collideEnemyCount > 0){
					//Cost increased based on frame number, the sooner the higher cost.
					int collisionCost = simulationFrame-frameRef;
					if(frameRef >= ignoreEnemyAfterFrameRef){
						//Do not care if enemy collision happen in far future (frame 30 onward)
						collisionCost = 0;
					}
					cost += collisionCost;
					//System.out.print(collisionCost+" ");
					firstCollisionCalculated = true;
				}
				
				//If player has high enough HP, AI will consider shooting
				if(info.player.getBaseHP() > 5 && frameRef >= shootableFrame){
					if(mainEnemy != null && 
							GameAI_v6.shouldShootThisEntity(playerFutures[frameRef].rect, playerFutures[frameRef].facingRight, 
							predictedMainEnemy[frameRef], mainEnemy.invul, mainEnemy.defender, true)){
						cost--;
						costReduced++;
						shootableFrame = frameRef + shootingDelay + 2;
					}
				}
			}
			//System.out.print(-costReduced);
			return cost;
		}
		
	}
	
	public class AIKeepDistanceTask implements AITask {

		@Override
		public AITask chooseTask(LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			if(info.enemy == null){
				return fightTask;
			}
			
			if(xInfo.accumulatedFrame > noDamageFrameThresholdForTransition){
				Rectangle screenRect = new Rectangle(0, 0, GameConstant.screenW, GameConstant.screenH);
				if(!screenRect.overlaps(info.enemyRect)){
					//Enemy is not visible, try to wander
					return wanderTask;
				}else{
					//Enemy is visible but stay invul/no defender/out of reach for too long without being "unbeatable", try to wander instead
					if(info.levelTimelimit < -10 && (xInfo.invulCounter > invulFrameThreshold || xInfo.outOfReachCounter > outOfReachThreshold)){
						return wanderTask;
					}
				}
			}
			float xDst = xCenterDst(info.playerRect, info.enemyRect);
			boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
			boolean shouldRunToward = false;
			if(isLargeEnemy(info.enemyRect)){
				float leftGap = getEnemyGap(info, true);
				float rightGap = getEnemyGap(info, false);
				if(enemyOnRight){
					shouldRunToward = rightGap > leftGap;
				}else{
					shouldRunToward = rightGap < leftGap;
				}
			}else{
				float myGap = getEnemyGap(info, enemyOnRight);
				shouldRunToward = myGap < 100;
			}
			
			if(xDst > 180){
				return fightTask;
			}else if(shouldRunToward){
				return runTowardTask;
			}
			return this;
		}

		@Override
		public ButtonCombination chooseCombination(ButtonCombination prevBtn,
				LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			if(info.enemy == null){
				return prevBtn;
			}

			//Search for best combination
			//System.out.print("Keep  ");
			int minCost = Integer.MAX_VALUE;
			List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
			//Loop through all possible button presses and pick the best
			for(ButtonCombination btn : ButtonCombination.values()){
				//System.out.print(btn+":(");
				int cost = calculateCombinationCost(btn, info.enemy, currentEnemies, predictedMainEnemy, predictedEnemies, info, delta);
				//System.out.print(")="+cost+"  ");
				if(cost <= minCost){
					if(cost < minCost){
						selectedCombination.clear();
						minCost = cost;
					}
					selectedCombination.add(btn);
				}
			}
			//System.out.println("");
			
			if(selectedCombination.size() == 1){
				return selectedCombination.get(0);
			}else{
				boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;
				
				//Priority: opposite direction > retain > random
				if(startFallingDown){
					//Falling down, only move to required direction
					if(enemyOnRight){
						if(selectedCombination.contains(ButtonCombination.Left) || selectedCombination.contains(ButtonCombination.LeftJump))
							return ButtonCombination.Left;
					}else{
						if(selectedCombination.contains(ButtonCombination.Right) || selectedCombination.contains(ButtonCombination.RightJump))
							return ButtonCombination.Right;
					}
				}else{
					if(prevBtn.jumpPressed()){
						//Jumping -- pick jump first
						if(enemyOnRight){
							if(selectedCombination.contains(ButtonCombination.LeftJump)){
								return ButtonCombination.LeftJump;
							}else if(selectedCombination.contains(ButtonCombination.Left)){
								return ButtonCombination.Left;
							}
						}else{
							if(selectedCombination.contains(ButtonCombination.RightJump)){
								return ButtonCombination.RightJump;
							}else if(selectedCombination.contains(ButtonCombination.Right)){
								return ButtonCombination.Right;
							}
						}
					}else{
						//On the floor -- try not to jump
						if(enemyOnRight){
							if(selectedCombination.contains(ButtonCombination.Left)){
								return ButtonCombination.Left;
							}else if(selectedCombination.contains(ButtonCombination.LeftJump)){
								return ButtonCombination.LeftJump;
							}
						}else{
							if(selectedCombination.contains(ButtonCombination.Right)){
								return ButtonCombination.Right;
							}else if(selectedCombination.contains(ButtonCombination.RightJump)){
								return ButtonCombination.RightJump;
							}
						}
					}
				}
				
				//Retain direction
				List<ButtonCombination> retained = new ArrayList<ButtonCombination>();
				for(ButtonCombination comb : selectedCombination){
					if(startFallingDown){
						//Falling
						if(comb.sameMovingDirectionAs(prevBtn)){
							return comb.nonJumpVersion();
						}
					}else{
						if(comb == prevBtn){
							return comb;
						}
						if(comb.sameMovingDirectionAs(prevBtn)){
							retained.add(comb);
						}
					}
				}
				if(retained.size() > 0){
					return retained.get( MathUtils.random(0,retained.size()-1) );
				}else{
					return selectedCombination.get( MathUtils.random(0,selectedCombination.size()-1) );
				}
			}
		}
		
		private int calculateCombinationCost(ButtonCombination inspecting, DynamicEntity mainEnemy, Rectangle[] currentEnemies, 
				Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, LevelSnapshot info, float delta){
			if(inspecting == ButtonCombination.None || inspecting == ButtonCombination.Jump){
				return 10000;
			}
			int cost = 0;
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(info.player);
			PlayerFutureState[] playerFutures = dummy.simulatePosition2(inspecting.leftPressed(), inspecting.rightPressed(), false, false, 
					inspecting.jumpPressed(), simulationFrame, delta);
			boolean firstCollideWallCalculated = false;
			boolean firstCollisionCalculated = false;
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
					if(frameRef < ignoreWallAfterFrameRef){
						cost += 100;
						//System.out.print("100 ");
					}
					firstCollideWallCalculated = true;
				}
				if(!firstCollisionCalculated && collideEnemyCount > 0){
					//Cost increased based on frame number, the sooner the higher cost.
					int collisionCost = simulationFrame-frameRef;
					if(frameRef >= ignoreEnemyAfterFrameRef){
						//Do not care if enemy collision happen in far future (frame 30 onward)
						collisionCost = 0;
					}
					//System.out.print(collisionCost+" ");
					cost += collisionCost;
					firstCollisionCalculated = true;
				}
			}
			return cost;
		}
	}
	
	public class AIRunTowardTask implements AITask {

		private int counter = 40;
		
		@Override
		public AITask chooseTask(LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			if(info.enemy == null){
				counter = 40;
				return fightTask;
			}
			if(counter <= 0){
				counter = 40;
				return fightTask;
			}else{
				counter--;
			}
			return this;
		}

		@Override
		public ButtonCombination chooseCombination(ButtonCombination prevBtn,
				LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			if(info.enemy == null){
				return prevBtn;
			}
			
			boolean enemyOnRight = info.playerRect.x < info.enemyRect.x;

			//Search for best combination
			int minCost = Integer.MAX_VALUE;
			//System.out.print("Toward  ");
			List<ButtonCombination> selectedCombination = new ArrayList<ButtonCombination>();
			//Loop through all possible button presses and pick the best
			for(ButtonCombination btn : ButtonCombination.values()){
				int cost = calculateCombinationCost(btn, info.enemy, currentEnemies, predictedMainEnemy, predictedEnemies, info, delta,
						!enemyOnRight);
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
			
			if(selectedCombination.size() == 1){
				return selectedCombination.get(0);
			}
			
			//Move to specified direction
			if(startFallingDown){
				//Falling down, only move toward enemy
				if(!enemyOnRight){
					if(selectedCombination.contains(ButtonCombination.Left) || selectedCombination.contains(ButtonCombination.LeftJump))
						return ButtonCombination.Left;
				}else{
					if(selectedCombination.contains(ButtonCombination.Right) || selectedCombination.contains(ButtonCombination.RightJump))
						return ButtonCombination.Right;
				}
			}else{
				if(!enemyOnRight){
					if(selectedCombination.contains(ButtonCombination.LeftJump)){
						return ButtonCombination.LeftJump;
					}else if(selectedCombination.contains(ButtonCombination.Left)){
						return ButtonCombination.Left;
					}
				}else{
					if(selectedCombination.contains(ButtonCombination.RightJump)){
						return ButtonCombination.RightJump;
					}else if(selectedCombination.contains(ButtonCombination.Right)){
						return ButtonCombination.Right;
					}
				}
			}
			
			//Retain direction
			List<ButtonCombination> retained = new ArrayList<ButtonCombination>();
			for(ButtonCombination comb : selectedCombination){
				if(startFallingDown){
					//Falling
					if(comb.sameMovingDirectionAs(prevBtn)){
						return comb.nonJumpVersion();
					}
				}else{
					if(comb == prevBtn){
						return comb;
					}
					if(comb.sameMovingDirectionAs(prevBtn)){
						retained.add(comb);
					}
				}
			}
			if(retained.size() > 0){
				return retained.get( MathUtils.random(0,retained.size()-1) );
			}else{
				return selectedCombination.get( MathUtils.random(0,selectedCombination.size()-1) );
			}
		}
		
		private int calculateCombinationCost(ButtonCombination inspecting, DynamicEntity mainEnemy, Rectangle[] currentEnemies, 
				Rectangle[] predictedMainEnemy, Rectangle[][] predictedEnemies, LevelSnapshot info, float delta, boolean forceLeft){
			if((forceLeft && !inspecting.leftPressed()) || (!forceLeft && inspecting.leftPressed())){
				return 10000;
			}
			int cost = 0;
			DummyPlayer dummy = new DummyPlayer(info.level, 1);
			dummy.mimicPlayer(info.player);
			PlayerFutureState[] playerFutures = dummy.simulatePosition2(inspecting.leftPressed(), inspecting.rightPressed(), false, false, 
					inspecting.jumpPressed(), simulationFrame, delta);
			boolean firstCollisionCalculated = false;
			for(int frameRef = 0; frameRef<playerFutures.length; frameRef++){
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
				
				if(!firstCollisionCalculated && collideEnemyCount > 0){
					//Cost increased based on frame number, the sooner the higher cost.
					int collisionCost = simulationFrame-frameRef;
					if(frameRef >= ignoreEnemyAfterFrameRef){
						//Do not care if enemy collision happen in far future (frame 30 onward)
						collisionCost = 0;
					}
					cost += collisionCost;
					firstCollisionCalculated = true;
				}
			}
			return cost;
		}
		
	}
	
	public class AIWanderTask implements AITask {

		private boolean wanderLeft = false;
		
		@Override
		public AITask chooseTask(LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			//No enemy, end wandering state immediately
			if(info.enemy == null)
				return fightTask;
			
			//Enemy's attack in proximity, end wandering state immediately
			for(Rectangle dynRect : info.tempRect.values()){
				if(dynRect.overlaps(info.playerRect)){
					return fightTask;
				}else{
					boolean xDstInProximity = xCenterDst(info.playerRect, dynRect) <=
							(info.playerRect.width+dynRect.width)/2f + threatProximityThreshold;
					boolean yDstInProximity = yCenterDst(info.playerRect, dynRect) <=
							(info.playerRect.height+dynRect.height)/2f + threatProximityThreshold;
					if(xDstInProximity && yDstInProximity){
						return fightTask;
					}
				}
			}
			
			//Visible enemy and it is either damagable now OR AI might collide with enemy --> end wandering
			Rectangle screenRect = new Rectangle(0, 0, GameConstant.screenW, GameConstant.screenH);
			if(screenRect.overlaps(info.enemyRect)){
				//Now damagable
				if(info.levelTimelimit < -10 && xInfo.invulCounter < invulFrameThreshold && 
						xInfo.outOfReachCounter < outOfReachThreshold){
					return fightTask;
				}
				//Not damagable but too close
				if(xCenterDst(info.playerRect, info.enemyRect) < (info.playerRect.width+info.enemyRect.width)/2f+GameConstant.tileSizeX*2 && 
						yCenterDst(info.playerRect, info.enemyRect) < (info.playerRect.height+info.enemyRect.height)/2f+GameConstant.tileSizeY*2){
					return keepDistanceTask;
				}
			}

			//Otherwise, continue
			return this;
		}

		@Override
		public ButtonCombination chooseCombination(ButtonCombination prevBtn,
				LevelSnapshot info, ExtraInfo xInfo,
				Rectangle[] currentEnemies, Rectangle[] predictedMainEnemy,
				Rectangle[][] predictedEnemies, float delta) {
			if(info.playerState.surfaceInFront[1]){
				//Wall on east
				wanderLeft = true;
			}else if(info.playerState.surfaceInFront[3]){
				//Wall on west
				wanderLeft = false;
			}
			if(wanderLeft){
				return ButtonCombination.LeftJump;
			}else{
				return ButtonCombination.RightJump;
			}
		}
		
	}
	
	private ExtraInfo extraInfo;
	private AITask currentTask;
	private ButtonCombination currentCombination;
	
	private AITask fightTask, keepDistanceTask, runTowardTask, wanderTask;
	
	private int stateChangeDelayCounter;
	private int shootDelayCounter;
	
	public GameAI_v6(){
		fightTask = new AIFightTask();
		keepDistanceTask = new AIKeepDistanceTask();
		runTowardTask = new AIRunTowardTask();
		wanderTask = new AIWanderTask();
		extraInfo = new ExtraInfo();
		currentTask = fightTask;
		currentCombination = ButtonCombination.None;
		stateChangeDelayCounter = 0;
		shootDelayCounter = 0;
	}
	
	@Override
	protected void onReset(DynamicEntity initialStateEnemy) {
		extraInfo.reset(initialStateEnemy);
		currentTask = fightTask;
		stateChangeDelayCounter = 0;
		shootDelayCounter = 0;
	}

	@Override
	protected void pressButton(Controller controller, LevelSnapshot info,
			float delta) {
		//Update additional info
		extraInfo.update(info);
		
		//Setup prediction data
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
		
		if(stateChangeDelayCounter <= 0){
			AITask newTask = currentTask.chooseTask(info, extraInfo, currentEnemies, predictedMainEnemy, predictedEnemies, delta);
			if(newTask != currentTask){
				currentTask = newTask;
				stateChangeDelayCounter = 40;
			}
		}else{
			stateChangeDelayCounter--;
		}
		currentCombination = currentTask.chooseCombination(currentCombination, info, extraInfo, 
				currentEnemies, predictedMainEnemy, predictedEnemies,  delta);
		controller.left = currentCombination.leftPressed();
		controller.right = currentCombination.rightPressed();
		//Require jumping, check if jumping should be delayed by 1 frame or not (release jumping button before pressing again)
		if(currentCombination.jumpPressed()){
			controller.jump = !startFallingDown;
		}
		if(shootDelayCounter <= 0){
			controller.shoot = GameAI_v6.shouldShootThisEntity(info, info.enemy, true);
			if(!controller.shoot){
				for(DynamicEntity dyn : info.tempRect.keySet()){
					if(GameAI_v6.shouldShootThisEntity(info, dyn, false)){
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
		
		if(info.levelTimelimit < -10){
			if(extraInfo.landHitOnEnemyFrame < 0){
				if(extraInfo.accumulatedFrame > noDamageFrameThreshold){
					controller.pause = true;
				}
			}else{
				if(extraInfo.accumulatedFrame - extraInfo.landHitOnEnemyFrame > noDamageFrameThreshold){
					controller.pause = true;
				}
			}
		}
	}

	//========================== Utility
	
	private static boolean isLargeEnemy(Rectangle enemyRect){
		return enemyRect.width >= 128 || enemyRect.height >= 128;
	}
	
	private static float getEnemyGap(LevelSnapshot info, boolean left){
		return left ? info.enemyRect.x - GameConstant.tileSizeX : 
			info.level.getStaticMap().getMapSize()[0] - GameConstant.tileSizeX - (info.enemyRect.x + info.enemyRect.width);
	}
	
	private static boolean isEnemyAbove(Rectangle playerRect, Rectangle enemyRect){
		return yCenterDiff(enemyRect, playerRect) > enemyRect.height/2 ;
	}
	
	private static boolean shouldShootThisEntity(LevelSnapshot info, DynamicEntity dyn, boolean isEnemy){
		if(dyn == null) return false;
		return shouldShootThisEntity(info.playerRect, info.player.direction.getX() > 0, dyn.getWorldCollider(), dyn.invul, dyn.defender,
				isEnemy);
	}
	
	private static boolean shouldShootThisEntity(Rectangle playerRect, boolean playerFaceRight, Rectangle dynRect, 
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
	
	private static boolean enemyIsInShootingRange(Rectangle playerRect, Rectangle dynRect){
		return yCenterDst(playerRect, dynRect) <= (dynRect.height+yDiffShootingMargin)/2f;
	}
	
	private static float xCenterDst(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return Math.abs(rect1.getCenter(v1).x - rect2.getCenter(v2).x);
	}
	
	private static float yCenterDst(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return Math.abs(rect1.getCenter(v1).y - rect2.getCenter(v2).y);
	}
	
	private static float yCenterDiff(Rectangle rect1, Rectangle rect2){
		Vector2 v1 = new Vector2(Vector2.Zero);
		Vector2 v2 = new Vector2(Vector2.Zero);
		return rect1.getCenter(v1).y - rect2.getCenter(v2).y;
	}
}

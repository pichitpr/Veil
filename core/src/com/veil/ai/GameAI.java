package com.veil.ai;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Rectangle;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;


public abstract class GameAI {
	
	public static GameAI instance = new GameAI_v4();
	
	//For debugging purpose
	public static Rectangle[] simulatedPlayerPos;
	public static Rectangle[][] predictedEnemyPos;
	public static Rectangle[] bufferedEnemyPos;
	
	//Available data
	protected HashMap<DynamicEntity, FrameHistoryBuffer> entityTracker = new HashMap<DynamicEntity, FrameHistoryBuffer>();
	protected boolean startFallingDown = false;
	
	public void aiUpdate(Controller controller, LevelSnapshot info, float delta){
		setupFrameDataAndFlag(info);
		if(GameConstant.profilingMode && !BattleProfile.instance.isStart())
			return;
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
	
	protected enum ButtonCombination {
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
	
	protected abstract void pressButton(Controller controller, LevelSnapshot info, float delta);
	
}

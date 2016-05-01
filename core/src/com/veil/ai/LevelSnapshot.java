package com.veil.ai;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.adl.EventFlag;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Player;

public class LevelSnapshot {
	
	public Rectangle playerRect;
	public EventFlag playerState;
	public boolean playerOnFloor;
	
	public Rectangle enemyRect;
	public Vector2 enemyDist;
	public HashMap<DynamicEntity,Rectangle> tempRect;
	public HashMap<DynamicEntity,Vector2> tempDist;
	
	public LevelSnapshot(Player player, List<DynamicEntity> permanent, List<DynamicEntity> temp){
		playerRect = new Rectangle(player.getWorldCollider());
		playerState = player.flag.cloneImportantFlag();
		playerOnFloor = player.onFloor();
		if(permanent.size() > 0){
			enemyRect = new Rectangle(permanent.get(0).getWorldCollider());
			enemyDist = new Vector2();
		}
		tempRect = new HashMap<DynamicEntity,Rectangle>();
		for(DynamicEntity dyn : temp){
			tempRect.put(dyn, new Rectangle(dyn.getWorldCollider()));
		}
	}
}

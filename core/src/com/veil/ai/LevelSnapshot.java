package com.veil.ai;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.veil.adl.EventFlag;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Player;

public class LevelSnapshot {
	
	public Player player;
	public Rectangle playerRect;
	public EventFlag playerState;
	public boolean playerOnFloor;
	public DynamicEntity enemy;
	public Rectangle enemyRect;
	public HashMap<DynamicEntity,Rectangle> tempRect;
	
	public LevelSnapshot(Player player, List<DynamicEntity> permanent, List<DynamicEntity> temp){
		this.player = player;
		playerRect = new Rectangle(player.getWorldCollider());
		playerState = player.flag.cloneImportantFlag();
		playerOnFloor = player.onFloor();
		if(permanent.size() > 0){
			enemy = permanent.get(0);
			enemyRect = new Rectangle(permanent.get(0).getWorldCollider());
		}
		tempRect = new HashMap<DynamicEntity,Rectangle>();
		for(DynamicEntity dyn : temp){
			tempRect.put(dyn, new Rectangle(dyn.getWorldCollider()));
		}
	}
}

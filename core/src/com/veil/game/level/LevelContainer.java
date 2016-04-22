package com.veil.game.level;

import java.util.List;

import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Player;

public interface LevelContainer {
	public GameMap getStaticMap(); //Get static entity
	public float getGravity();
	public Player getPlayer();
	public List<DynamicEntity> getPermanentDynamicEntity();
	public void pendingSpawn(DynamicEntity dyn);
	public boolean canHandleMoreEntity();
	public List<DynamicEntity> getTemporaryDynamicEntity();
	
}

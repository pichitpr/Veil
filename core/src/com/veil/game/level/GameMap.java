package com.veil.game.level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.element.DynamicEntity;

public interface GameMap {
	public int[] getMapSize();
	public void resolveEnvironmentCollisionFor(DynamicEntity dyn, Vector2 lastPos);
	public void update(float delta);
	public void render(SpriteBatch batch,AtlasRegion[] atlas);
}

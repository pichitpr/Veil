package com.veil.game.level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.collider.CollisionResolver;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Wall;

public class PieceMap implements GameMap{

	private Wall[] environment;
	
	public PieceMap(){
		environment = new Wall[3];
		environment[0] = new Wall(new Rectangle(100,300,400,80),3);
		environment[1] = new Wall(new Rectangle(120,250,60,300),3);
		environment[2] = new Wall(new Rectangle(0,-50,500,100),3);
	}
	
	public Wall[] getEnvironment(){
		return environment;
	}
	
	public void update(float delta){
		for(Wall w : environment){
			w.update(delta);
		}
	}
	
	public void render(SpriteBatch batch,AtlasRegion[] atlas){
		for(Wall w : environment){
			w.render(batch, atlas);
		}
	}

	@Override
	public void resolveEnvironmentCollisionFor(DynamicEntity dyn, Vector2 lastPos) {
		for(Wall w : environment){
			CollisionResolver.resolveSweepNoPenetrate(w, dyn, lastPos);
		}
	}

	@Override
	public int[] getMapSize() {
		return new int[]{800,600};
	}
}

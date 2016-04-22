package com.veil.game.element;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.GameConstant;

/**
 * Entity operates in world coordinate system ONLY. DynamicEntity must not interact with
 * this class directly
 */
public abstract class Entity {
	
	//private static Rectangle tmp = new Rectangle();
	private Rectangle collider; //Store world coordinate of entity
	protected int texture;
	protected boolean visible = true;
	
	public Entity(Rectangle collider, int texture){
		this.collider = collider;
		setTexture(texture);
	}
	
	/*
	public Vector2 getPosition(){
		return collider.getPosition(new Vector2());
	}
	*/
	
	public void setTexture(int texture){
		this.texture = texture;
	}
	
	public void setRectangle(int width, int height){
		this.collider.width = width;
		this.collider.height = height;
	}
	
	/**
	 * Get BL-based world position
	 */
	public Rectangle getWorldCollider(){
		return collider;
	}
	
	/**
	 * Get center-based world position
	 */
	public Vector2 getWorldCenteredPosition(){
		return new Vector2(this.collider.x+this.collider.width/2f , 
				this.collider.y+this.collider.height/2f);
	}
	
	/**
	 * Set center-based world position
	 */
	public void setWorldCenteredPosition(float x,float y){
		collider.x = x-this.collider.width/2f;
		collider.y = y-this.collider.height/2f;
	}
	
	public void render(SpriteBatch batch,AtlasRegion[] atlas){
		if(visible)
			batch.draw(atlas[texture],collider.x,collider.y,collider.width,collider.height);
	}
	
	public void update(float delta){
	}
	
	public boolean completelyOutOfScreen(){
		return collider.x < -collider.width || collider.x >= GameConstant.screenW || 
				collider.y < -collider.height || collider.y >= GameConstant.screenH;
	}
	
	/*
	public boolean collideWith(Entity e){
		return Intersector.intersectRectangles(collider, e.collider, new Rectangle());
	}
	
	public void pushEntityOut(Entity e, Vector2 lastPosition){
		if(Intersector.intersectRectangles(collider, e.collider, tmp)){
			
			//System.out.println("X "+tmp.getWidth()+" Y "+tmp.getHeight());
			Vector2 vec = lastPosition.sub(e.getPosition());
			float x = vec.x > 0 ? 1 : -1;
			float y = vec.y > 0 ? 1 : -1;
			if(tmp.getWidth() < tmp.getHeight()){
				//Push X
				e.collider.setPosition(e.collider.getX()+x*tmp.getWidth(), e.collider.getY());
			}else{
				//Push Y
				e.collider.setPosition(e.collider.getX(), e.collider.getY()+y*tmp.getHeight());
			}
			
			
			Vector2 stepVector = lastPosition.sub(e.getPosition()).scl(0.05f);
			while(Intersector.intersectRectangles(collider, e.collider, tmp)){
				e.collider.setPosition(e.getPosition().add(stepVector));
			}
		}
	}
	*/
}

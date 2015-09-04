package com.veil.game.element;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.adl.EventFlag;
import com.veil.adl.literal.Direction;
import com.veil.game.GameConstant;
import com.veil.game.level.LevelContainer;

public abstract class DynamicEntity extends Entity{

	public enum Group{
		ALLY , ENEMY , HOSTILE
	};
	
	protected LevelContainer level;
	protected Vector2 lastPos; //For collision resolution, require world ref
	protected float lastVy;
	protected int lastHP;
	
	public String identifier;
	public Direction direction = new Direction(Direction.EAST);
	public Group group = Group.ENEMY;
	public int maxhp;
	protected int hp = 1;
	public int atk;
	public boolean projectile;
	public boolean phasing,invul;//,rigid;
	public boolean attacker,defender;
	
	public float vx,vy,gravityVy;
	protected float gravityEff;
	protected boolean reachFloor;
	
	//public EntityHierarchy hierarchy;
	public DynamicEntity parent;
	public List<DynamicEntity> children;
	private boolean parentPart;
	private Vector2 relativePos; //From center to center
	
	public EventFlag flag;
	private int outScreenTimeCount = 0;
	
	public DynamicEntity(LevelContainer level, Rectangle collider, int texture) {
		super(collider, texture);
		this.identifier = "^";
		this.level = level;
		lastPos = collider.getPosition(new Vector2());
		this.parent = null;
		this.children = new ArrayList<DynamicEntity>();
		this.parentPart = false;
		this.relativePos = null;
		flag = new EventFlag();
	}
	
	public DynamicEntity(LevelContainer level, Rectangle collider, int texture, 
			ScriptedEntity parent, boolean parentPart, Vector2 relativePos){
		super(collider, texture);
		this.identifier = "^";
		this.level = level;
		lastPos = collider.getPosition(new Vector2());
		this.parent = parent;
		this.parent.children.add(this);
		this.children = new ArrayList<DynamicEntity>();
		this.parentPart = parentPart;
		this.relativePos = relativePos;
		flag = new EventFlag();
	}
	
	public DynamicEntity getBaseEntity(){
		if(this.parentPart){
			return parent.getBaseEntity();
		}else
			return this;
	}
	
	public boolean shouldBeRemovedFromWorld(){
		return hp <= 0;
	}
	
	public int getBaseHP(){
		if(parentPart)
			return getBaseEntity().getBaseHP();
		else
			return hp;
	}
	
	public Vector2 getCenteredPositionCorrespondToRF(){
		if(useRelativePos()){
			return new Vector2(relativePos);
		}else{
			return getWorldCenteredPosition();
		}
	}
	
	/**
	 * Set center of the entity to the position pos according to its reference frame
	 */
	public void setCenteredPositionCorrespondToRF(Vector2 pos){
		if(useRelativePos()){
			relativePos.set(pos);
		}else{
			setWorldCenteredPosition(pos.x, pos.y);
		}
	}
	
	public Vector2 getLastPos(){
		return lastPos;
	}
	
	public float getGravityEff(){
		return gravityEff;
	}
	
	public boolean isParentPart(){
		return parentPart;
	}
	
	protected boolean useRelativePos(){
		return (relativePos != null && parent != null);
	}
	
	public boolean isGravityEffectDownward(){
		return gravityEff*level.getGravity() > 0;
	}
	
	public void despawn(){
		this.hp = 0;
	}
	
	public int setBaseHP(int hp){
		if(parentPart)
			return getBaseEntity().hp = hp;
		else
			return this.hp = hp;
	}
	
	public void setVx(float vx){
		this.vx = vx;
	}
	
	public void setVy(float vy){
		this.vy = vy;
	}
	
	public void setGravityVy(float vy){
		this.gravityVy = vy;
	}
	
	public void addVx(float vx){
		this.vx += vx;
	}
	
	public void addVy(float vy){
		this.vy += vy;
	}
	
	public void addGravityVy(float vy){
		this.gravityVy += vy;
	}
	
	public void setGravityEff(float eff){
		this.gravityEff = eff;
	}
	
	@Override
	public void update(float delta){
		if(level.getGravity()*gravityEff == 0 || reachFloor){
			gravityVy = 0;
		}
		
		getWorldCollider().getPosition(lastPos);
		
		lastVy = gravityVy+vy;
		lastHP = getBaseHP();
		
		gravityVy -= gravityEff*level.getGravity();
		behaviorUpdate(delta);
		setCenteredPositionCorrespondToRF(
				getCenteredPositionCorrespondToRF().add(vx, vy+gravityVy) );
		if(useRelativePos()){
			//Update world position for entity with relativePos
			setWorldCenteredPosition(parent.getWorldCenteredPosition().x+relativePos.x, 
					parent.getWorldCenteredPosition().y+relativePos.y );
		}
		postBehaviorUpdate(delta);
		
		reachFloor = false;
		
		if(completelyOutOfScreen()){
			outScreenTimeCount++;
		}else{
			outScreenTimeCount = 0;
		}
		if(outScreenTimeCount > GameConstant.autoDespawnTime){
			despawn();
		}
	}
	
	public void reachFloor(){
		reachFloor = true;
	}
	
	public boolean isOtherCanPass(DynamicEntity other){
		//TODO: Add rigid/phasing here
		return true;
	}
	
	//Called before vx & vy is applied to entity (before moving)
	//Also, gravity is applied to vy before calling this method
	public abstract void behaviorUpdate(float delta);
	//Called after moving, before floor touching & static collision handling
	public abstract void postBehaviorUpdate(float delta);
	//Called after all collision response resolved
	public abstract void handleCollisionEvent();
	//Called on being despawn (before removing from entity list)
	public abstract void onDespawn(float delta);
	
	@Override
	public void render(SpriteBatch batch,AtlasRegion[] atlas){
		if(visible){
			Color c = batch.getColor();
			
			if(!defender){
				batch.setColor(c.r, c.g, c.b, 0.6f);
			}
			Rectangle collider = getWorldCollider();
			batch.draw(atlas[texture],collider.x,collider.y,collider.width,collider.height);
			
			batch.setColor(c);
		}
	}
}

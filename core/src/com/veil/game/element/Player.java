package com.veil.game.element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.veil.game.GameConstant;
import com.veil.game.level.LevelContainer;

public class Player extends DynamicEntity{
	
	//TODO: Enable 2axis simultaneous move for shooting genre!!
	//TODO: Enable fixed facing movement
	private int floorStunDuration = 0;
	
	private boolean pressJump = false;
	private int timeCount = 0;
	private int invulFrame = 0; //TODO: combine invul frame with invuk flag for dyn
	private boolean pressShoot = false;
	
	public Player(LevelContainer level, int texture) {
		super(level, new Rectangle(50,500,32,48), texture);
		group = Group.ALLY;
		defender = true;
		gravityEff = 1;
	}

	@Override
	public void behaviorUpdate(float delta){
		if(invulFrame % 6 == 0)
			visible = true;
		else if(invulFrame % 6 == 3)
			visible = false;
		
		if(floorStunDuration > 0){
			floorStunDuration--;
			return;
		}
		
		if(!GameConstant.isPlaformer){
			if(Gdx.input.isKeyPressed(Input.Keys.UP)){
				vy = GameConstant.speed;
				this.direction.setDirection(0, 1);
			}else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				vy = -GameConstant.speed;
				this.direction.setDirection(0, -1);
			}
		}else{
			if(Gdx.input.isKeyPressed(Input.Keys.Z)){
				if(!pressJump && reachFloor){
					pressJump = true;
					timeCount = GameConstant.jumpCounter;
				}
				if(timeCount > 0){
					timeCount--;
					gravityVy += GameConstant.jumpSpeed*gravityEff*level.getGravity();
				}
			}else{
				pressJump = false;
				timeCount = 0;
			}
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			vx = -GameConstant.speed;
			this.direction.setDirection(-1, 0);
		}else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			vx = GameConstant.speed;
			this.direction.setDirection(1, 0);
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.X)){
			if(!pressShoot){
				level.pendingSpawn(new ScriptedEntity(level, "Bullet_Player"));
				pressShoot = true;
			}
		}else{
			pressShoot = false;
		}
	}

	@Override
	public void postBehaviorUpdate(float delta) {
		vx = 0;
		vy = 0;
	}

	@Override
	public void handleCollisionEvent() {
		if(invulFrame > 0)
			invulFrame--;
		
		//Handle as defender
		DynamicEntity dyn;
		for(Entity e : flag.collidingEntity){
			if(e instanceof DynamicEntity){
				flag.collideDynamic = true;
				dyn = (DynamicEntity)e; 
				if(dyn.attacker && dyn.group != Group.ALLY){
					//Inflict damage
					if(!this.invul && invulFrame <= 0){
						dyn.flag.damage = true;
						this.hp -= dyn.atk;
						if(this.hp < 0)
							this.hp = 0;
						invulFrame = 60;
					}
					
					//Projectile attacker
					if(dyn.projectile && !dyn.invul){
						dyn.hp -= 1;
						if(dyn.hp < 0)
							dyn.hp = 0;
					}
					
					dyn.flag.attack = true;
					flag.attacked = true;
				}
			}
		}
		
		if(gravityEff > 0){
			if(lastVy >= 0 && gravityVy+vy < 0) flag.reachJumpingPeak = true;
		}else if(gravityEff < 0){
			if(lastVy <= 0 && gravityVy+vy > 0) flag.reachJumpingPeak = true;
		}
		if(lastHP < getBaseHP()){
			flag.damaged = true;
		}
	}

	@Override
	public void onDespawn(float delta){
	}
	
	public void floorStun(int duration){
		if((gravityEff > 0 && flag.surfaceInFront[2]) || 
				(gravityEff < 0 && flag.surfaceInFront[0])){
			floorStunDuration = duration;
		}
	}
	
}
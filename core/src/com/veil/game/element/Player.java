package com.veil.game.element;

import com.badlogic.gdx.math.Rectangle;
import com.veil.ai.BattleProfile;
import com.veil.ai.Controller;
import com.veil.game.GameConstant;
import com.veil.game.level.LevelContainer;

public class Player extends DynamicEntity{
	
	//TODO: Enable 2axis simultaneous move for shooting genre!!
	//TODO: Enable fixed facing movement
	protected int floorStunDuration = 0;
	
	protected boolean pressJump = false;
	protected int timeCount = 0;
	protected int invulFrameCounter = 0;
	protected boolean pressShoot = false;
	private int shootingFrameCounter = 0;
	
	public Player(LevelContainer level, int texture) {
		super(level, new Rectangle(50,500,32,48), texture);
		group = Group.ALLY;
		defender = true;
		gravityEff = 1;
		invulFrame = 60;
	}

	@Override
	public void behaviorUpdate(float delta){
		if(invulFrameCounter % 6 == 0)
			visible = true;
		else if(invulFrameCounter % 6 == 3)
			visible = false;
		
		if(floorStunDuration > 0){
			floorStunDuration--;
			return;
		}
		
		if(!GameConstant.isPlaformer){
			if(Controller.instance.up){
				vy = GameConstant.speed;
				this.direction.setDirection(0, 1);
			}else if(Controller.instance.down){
				vy = -GameConstant.speed;
				this.direction.setDirection(0, -1);
			}
		}else{
			if(Controller.instance.jump){
				startJump();
				if(timeCount > 0){
					timeCount--;
					gravityVy += GameConstant.jumpSpeed*gravityEff*level.getGravity();
				}
			}else{
				pressJump = false;
				timeCount = 0;
			}
		}
		
		if(Controller.instance.left){
			vx = -GameConstant.speed;
			this.direction.setDirection(-1, 0);
		}else if(Controller.instance.right){
			vx = GameConstant.speed;
			this.direction.setDirection(1, 0);
		}
		
		if(shootingFrameCounter > 0)
			shootingFrameCounter--;
		if(Controller.instance.shoot){
			if(!pressShoot && shootingFrameCounter <= 0){
				level.pendingSpawn(new ScriptedEntity(level, "Bullet_Player"));
				pressShoot = true;
				shootingFrameCounter = 10;
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
		if(invulFrameCounter > 0)
			invulFrameCounter--;
		
		//Handle as defender
		DynamicEntity dyn;
		for(Entity e : flag.collidingEntity){
			if(e instanceof DynamicEntity){
				flag.collideDynamic = true;
				dyn = (DynamicEntity)e; 
				if(dyn.attacker && dyn.group != Group.ALLY){
					//Inflict damage
					if(!this.invul && invulFrameCounter <= 0){
						dyn.flag.damage = true;
						this.hp -= dyn.atk;
						if(this.hp < 0)
							this.hp = 0;
						invulFrameCounter = invulFrame;
					}
					
					//Projectile attacker
					if(dyn.projectile && !dyn.invul){
						dyn.hp -= 1;
						if(dyn.hp < 0)
							dyn.hp = 0;
					}
					
					dyn.flag.attack = true;
					flag.attacked = true;
					
					//Profiling
					BattleProfile.instance.hitPlayer(dyn);
				}
			}
		}
		
		if(flag.jumping){
			if(gravityEff > 0){
				if(lastVy >= 0 && gravityVy+vy < 0) flag.reachJumpingPeak = true;
			}else if(gravityEff < 0){
				if(lastVy <= 0 && gravityVy+vy > 0) flag.reachJumpingPeak = true;
			}
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
	
	private boolean jumpOnce = false;
	private void startJump(){
		if(!pressJump && reachFloor){
			//Range profiling mode allow jumping only once per battle
			if(GameConstant.profilingMode && GameConstant.rangeProfiling){
				if(jumpOnce){
					return;
				}else{
					//First jump, save distance profile
				}
			}
			flag.jumping = true;
			pressJump = true;
			timeCount = GameConstant.jumpCounter;
			jumpOnce = true;
		}
	}
}
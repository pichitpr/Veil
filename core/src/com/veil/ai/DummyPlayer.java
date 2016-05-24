package com.veil.ai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.adl.literal.Direction;
import com.veil.game.GameConstant;
import com.veil.game.element.Player;
import com.veil.game.level.LevelContainer;

/**
 * A player used to simulate actual player movement based on button pressed
 */
public class DummyPlayer extends Player{

	private Controller controller = new Controller();
	
	public DummyPlayer(LevelContainer level, int texture) {
		super(level, texture);
	}

	@Override
	public void behaviorUpdate(float delta){		
		if(!GameConstant.isPlaformer){
			if(controller.up){
				vy = GameConstant.speed;
				this.direction.setDirection(0, 1);
			}else if(controller.down){
				vy = -GameConstant.speed;
				this.direction.setDirection(0, -1);
			}
		}else{
			if(controller.jump){
				if(!pressJump && reachFloor){
					flag.jumping = true;
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
		
		if(controller.left){
			vx = -GameConstant.speed;
			this.direction.setDirection(-1, 0);
		}else if(controller.right){
			vx = GameConstant.speed;
			this.direction.setDirection(1, 0);
		}
	}
	
	@Override
	public void handleCollisionEvent() {}
	
	@Override
	public void floorStun(int duration){}
	
	/**
	 * Copy player current state for simulation
	 */
	public void mimicPlayer(Player player){
		this.flag = player.flag.cloneImportantFlag();
		this.direction = new Direction(player.direction);
		this.gravityEff = player.getGravityEff();
		this.gravityVy = player.gravityVy;
		this.lastPos = new Vector2(player.getLastPos());
		this.reachFloor = player.onFloor();
		Rectangle rect = player.getWorldCollider();
		this.setRectangle((int)rect.width, (int)rect.height);
		Vector2 playerPos = player.getWorldCenteredPosition();
		this.setWorldCenteredPosition(playerPos.x, playerPos.y);
	}
	
	/**
	 * Simulate player position from buttons pressed using dummy in current state and starting point. 
	 * It is assumed that controller buttons are pressed for "futureFrame" frames without any button released in-between.
	 */
	public Rectangle[] simulatePosition(boolean left, boolean right, boolean up, boolean down, boolean jump, 
			int futureFrame, float simulationDelta){
		controller.left = left;
		controller.right = right;
		controller.up = up;
		controller.down = down;
		controller.jump = jump;
		Rectangle[] result = new Rectangle[futureFrame];
		for(int i=1; i<=futureFrame; i++){
			this.update(simulationDelta);
			this.flag.clear();
			this.level.getStaticMap().resolveEnvironmentCollisionFor(this, this.getLastPos());
			result[i-1] = new Rectangle(this.getWorldCollider());
		}
		return result;
	}
}

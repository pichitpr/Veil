package com.veil.game.collider;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.GameConstant;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Entity;
import com.veil.game.level.TiledMap;

/*
 * This class provides utility method to correct the position of dynamic entities after update
 * 
 * resolve(Entity stillEntity, DynamicEntity movingEntity, Vector2 lastPos)
 * Resolve movingEntity's position against specified static entity stillEntity.
 * Last position of movingEntity lastPos is required
 * No resolution applied if movingEntity has phasing turn on 
 * TODO: Also set collision flag if collision occur
 * 
 * resolve(TiledMap map, DynamicEntity dyn, Vector2 lastPos)
 * Resolve dyn's position against the map (every static entities).
 * dyn's last position is required
 * No resolution applied if dyn has phasing turn on
 * TODO: Also set collision flag if collision occur
 * 
 * resolve(DynamicEntity dyn1, Vector2 lastPos1, DynamicEntity dyn2, Vector2 lastPos2)
 * Resolve position of a pair of DynamicEntity (beware! each entity has its own passable list) 
 * Note down the colliding entity if collision occurs.
 * 
 * NOTE: beware of "still" dynamic entity
 */
public class CollisionResolver {

	private static Rectangle rect = new Rectangle();
	private static Vector2 vec = new Vector2();
	private static Vector2 vec2 = new Vector2();
	
	private static Rectangle still;
	private static Rectangle moving;
	
	public static void resolveSweepNoPenetrate(Entity stillEntity, DynamicEntity movingEntity, Vector2 previousPos){
		still = stillEntity.getWorldCollider();
		moving = movingEntity.getWorldCollider();
		if(Intersector.overlaps(still, moving)){
			if(movingEntity.phasing) return;
			vec2.set(previousPos);
			vec2.sub(moving.getPosition(vec)).scl(0.05f);
			if(vec2.isZero(0.005f)) return;
			//System.out.println(still.toString());
			//System.out.println(previousPos.toString());
			
			rect.set(moving);
			while(Intersector.overlaps(still, rect)){
				rect.setPosition(rect.getPosition(vec).add(vec2));
			}
			float resolvedX = moving.getX();
			float resolvedY = moving.getY();
			
			//Find colliding surface, fix character position to the surface
			if(Math.abs(rect.y-(still.y+still.height)) <= 1){
				//System.out.println("DOWN "+still.toString());
				//moving.setPosition(moving.getX(), rect.getY());
				resolvedY = rect.getY();
				if(movingEntity.isGravityEffectDownward())
					movingEntity.reachFloor();
				else{
					movingEntity.setVy(0);
					movingEntity.setGravityVy(0);
				}
				movingEntity.flag.surfaceInFront[2] = true; //SOUTH surface
			}else{
				if(Math.abs(rect.y+rect.height-still.y) <= 1){
					//System.out.println("UP "+still.toString());
					//moving.setPosition(moving.getX(), rect.getY());
					resolvedY = rect.getY();
					if(!movingEntity.isGravityEffectDownward())
						movingEntity.reachFloor();
					else{
						movingEntity.setVy(0);
						movingEntity.setGravityVy(0);
					}
					movingEntity.flag.surfaceInFront[0] = true; //NORTH surface
				}
			}
			
			if(Math.abs(rect.x-(still.x+still.width)) <= 1){
				//System.out.println("LEFT "+still.toString());
				//moving.setPosition(rect.getX(), moving.getY());
				resolvedX = rect.getX();
				movingEntity.flag.surfaceInFront[3] = true; //WEST surface
			}else if(Math.abs(rect.x+rect.width-still.x) <= 1){
				//System.out.println("RIGHT "+still.toString());
				//moving.setPosition(rect.getX(), moving.getY());
				resolvedX = rect.getX();
				movingEntity.flag.surfaceInFront[1] = true; //EAST surface
			}
			moving.setPosition(resolvedX, resolvedY);
		}
	}
	
	//TODO: Fixed bug here -- not use now
	public static void resolveSweep(Entity stillEntity, DynamicEntity movingEntity, Vector2 previousPos){
		still = stillEntity.getWorldCollider();
		moving = movingEntity.getWorldCollider();
		vec2.set(previousPos);
		vec2.sub(moving.getPosition(vec));
		float moveLen = vec2.len();
		vec2.scl(0.05f);
		if(vec2.isZero(0.005f)) return;
		if(!Intersector.overlaps(still, moving)){
			float shortSize = (still.width > still.height ? still.height : still.width);
			//shortSize += (moving.width > moving.height ? moving.height : moving.width);
			if(shortSize > moveLen){
				//Not overlap & No penetration can occur
				return;
			}
			shortSize = vec2.len();
			rect.set(moving);
			while(!Intersector.overlaps(still, rect) && moveLen > 0){
				moveLen -= shortSize;
				rect.setPosition(rect.getPosition(vec).add(vec2));
			}
			if(moveLen <= 0)
				return; //No penetration
			System.out.println("PENs");
		}else{
			rect.set(moving);
		}
		//System.out.println(still.toString());
		//System.out.println(previousPos.toString());
		while(Intersector.overlaps(still, rect)){
			rect.setPosition(rect.getPosition(vec).add(vec2));
		}
		float resolvedX = moving.getX();
		float resolvedY = moving.getY();
		
		//Find colliding surface, fix character position to the surface
		if(Math.abs(rect.y-(still.y+still.height)) <= 1){
			//System.out.println("DOWN "+still.toString());
			//moving.setPosition(moving.getX(), rect.getY());
			resolvedY = rect.getY();
			if(movingEntity.isGravityEffectDownward())
				movingEntity.reachFloor();
			else{
				movingEntity.setVy(0);
				movingEntity.setGravityVy(0);
			}
		}else{
			if(Math.abs(rect.y+rect.height-still.y) <= 1){
				//System.out.println("UP "+still.toString());
				//moving.setPosition(moving.getX(), rect.getY());
				resolvedY = rect.getY();
				if(!movingEntity.isGravityEffectDownward())
					movingEntity.reachFloor();
				else{
					movingEntity.setVy(0);
					movingEntity.setGravityVy(0);
				}
			}
		}
		
		if(Math.abs(rect.x-(still.x+still.width)) <= 1){
			//System.out.println("LEFT "+still.toString());
			//moving.setPosition(rect.getX(), moving.getY());
			resolvedX = rect.getX();
		}else if(Math.abs(rect.x+rect.width-still.x) <= 1){
			//System.out.println("RIGHT "+still.toString());
			//moving.setPosition(rect.getX(), moving.getY());
			resolvedX = rect.getX();
		}
		moving.setPosition(resolvedX, resolvedY);
	}
	
	/*
	public static void resolveTiledBased(TileMap map, Entity entity, float vx, float vy){
		moving = entity.getCollider();
		
		int startX = MathUtils.floor(moving.x) / GameConstant.tileSizeX;
		int startY = MathUtils.floor(moving.y) / GameConstant.tileSizeY;
		int endX = MathUtils.floor(moving.x+moving.width-1) / GameConstant.tileSizeX;
		int endY = MathUtils.floor(moving.y+moving.height-1) / GameConstant.tileSizeY;
		System.out.println("["+startX+","+startY+"] - ["+endX+","+endY+"]");
		if(map.outOfMap(startX, startY, endX, endY)) return;
		
		int x,y;
		
		if(vx < 0){
			for(y=startY; y<=endY; y++){
				if(map.getTiles()[y][startX] != 0){
					moving.x = (startX+1)*GameConstant.tileSizeX;
					break;
				}
			}
		}else if(vx > 0){
			for(y=startY; y<=endY; y++){
				if(map.getTiles()[y][endX] != 0){
					moving.x = endX*GameConstant.tileSizeX-moving.width;
					break;
				}
			}
		}
		
		startX = MathUtils.floor(moving.x) / GameConstant.tileSizeX;
		endX = MathUtils.ceil(moving.x+moving.width) / GameConstant.tileSizeX;
		
		if(vy < 0){
			for(x=startX; x<=endX; x++){
				if(map.getTiles()[startY][x] != 0){
					moving.y = (startY+1)*GameConstant.tileSizeY;
					break;
				}
			}
		}else if(vy > 0){
			for(x=startX; x<=endX; x++){
				if(map.getTiles()[endY][x] != 0){
					moving.y = endY*GameConstant.tileSizeY-moving.height;
					break;
				}
			}
		}
	}
	*/
	
	/*
	public static void resolveSingle(Entity stillEntity, DynamicEntity dyn, Vector2 lastPos){
		still = stillEntity.getCollider();
		moving = dyn.getCollider();
		float dx = moving.x - lastPos.x;
		float dy = moving.y - lastPos.y;
		
		
		if(still.x+still.width < lastPos.x){
			//Dyn on the right
		}else if(still.x > lastPos.x+moving.width){
			//Dyn on the left
		}else{
			//X Overlap during last position
		}
		
		if(still.y+still.height < lastPos.y){
			//Dyn above
		}else if(still.y > lastPos.y+moving.height){
			//Dyn below
		}else{
			//Y Overlap during last position
		}
	}
	*/
	
	public static void resolveTiledBased(TiledMap map, DynamicEntity dyn, Vector2 lastPos){
		moving = dyn.getWorldCollider();
		int startX = MathUtils.floor(lastPos.x) / GameConstant.tileSizeX;
		int startY = MathUtils.floor(lastPos.y) / GameConstant.tileSizeY;
		int endX = MathUtils.floor(lastPos.x+moving.width-1) / GameConstant.tileSizeX;
		int endY = MathUtils.floor(lastPos.y+moving.height-1) / GameConstant.tileSizeY;
		
		if(map.outOfMap(startX, startY, endX, endY) || dyn.phasing) return;
		
		float dx = moving.x - lastPos.x;
		float dy = moving.y - lastPos.y;
		float target;
		int x,y;
		boolean breakLoop;
		
		if(dx < 0){
			//Move left
			dx = -dx;
			for(x=startX-1; x>=0; x--){
				breakLoop = false;
				for(y=startY; y<=endY; y++){
					if(map.getTiles()[y][x] > 0){
						breakLoop = true;
						break;
					}
				}
				if(breakLoop) break;
			}
			target = (x+1)*GameConstant.tileSizeX;
			if(lastPos.x - target < dx){
				dyn.flag.surfaceInFront[3] = true;
				moving.x = target;
			}
		}else if(dx > 0){
			//Move right
			for(x=startX+1; x<map.getTiles()[0].length; x++){
				breakLoop = false;
				for(y=startY; y<=endY; y++){
					if(map.getTiles()[y][x] > 0){
						breakLoop = true;
						break;
					}
				}
				if(breakLoop) break;
			}
			target = x*GameConstant.tileSizeX-moving.width;
			if(target - lastPos.x < dx){
				dyn.flag.surfaceInFront[1] = true;
				moving.x = target;
			}
		}
		
		if(dy < 0){
			//Move down - Feet collision
			dy = -dy;
			for(y=startY-1; y>=0; y--){
				breakLoop = false;
				for(x=startX; x<=endX; x++){
					if(map.getTiles()[y][x] != 0){
						breakLoop = true;
						break;
					}
				}
				if(breakLoop) break;
			}
			target = (y+1)*GameConstant.tileSizeY;
			if(lastPos.y - target < dy){
				dyn.flag.surfaceInFront[2] = true;
				moving.y = target;
				if(dyn.isGravityEffectDownward())
					dyn.reachFloor();
				else{
					dyn.setVy(0);
					dyn.setGravityVy(0);
				}
			}
		}else if(dy > 0){
			//Move up - Head collision
			for(y=startY+1; y<map.getTiles().length; y++){
				breakLoop = false;
				for(x=startX; x<=endX; x++){
					if(map.getTiles()[y][x] != 0 && map.getTiles()[y][x] != -1){
						breakLoop = true;
						break;
					}
				}
				if(breakLoop) break;
			}
			target = y*GameConstant.tileSizeY-moving.height;
			if(target - lastPos.y < dy){
				dyn.flag.surfaceInFront[0] = true;
				moving.y = target;
				if(dyn.isGravityEffectDownward()){
					dyn.setVy(0);
					dyn.setGravityVy(0);
				}else
					dyn.reachFloor(); //Treat ceiling as floor if inverse gravity
			}
		}
	}
	
	//TODO:Change to real dynamic VS dynamic
	private static void resolveDynamic(Entity stillEntity, DynamicEntity movingEntity, Vector2 previousPos){
		still = stillEntity.getWorldCollider();
		moving = movingEntity.getWorldCollider();
		if(Intersector.overlaps(still, moving)){
			if(movingEntity.phasing) return;
			vec2.set(previousPos);
			vec2.sub(moving.getPosition(vec)).scl(0.05f);
			if(vec2.isZero(0.005f)) return;
			//System.out.println(still.toString());
			//System.out.println(previousPos.toString());
			rect.set(moving);
			while(Intersector.overlaps(still, rect)){
				rect.setPosition(rect.getPosition(vec).add(vec2));
			}
			float resolvedX = moving.getX();
			float resolvedY = moving.getY();
			
			//Find colliding surface, fix character position to the surface
			if(Math.abs(rect.y-(still.y+still.height)) <= 1){
				//System.out.println("DOWN "+still.toString());
				//moving.setPosition(moving.getX(), rect.getY());
				resolvedY = rect.getY();
				if(movingEntity.isGravityEffectDownward())
					movingEntity.reachFloor();
				else{
					movingEntity.setVy(0);
					movingEntity.setGravityVy(0);
				}
			}else{
				if(Math.abs(rect.y+rect.height-still.y) <= 1){
					//System.out.println("UP "+still.toString());
					//moving.setPosition(moving.getX(), rect.getY());
					resolvedY = rect.getY();
					if(!movingEntity.isGravityEffectDownward())
						movingEntity.reachFloor();
					else{
						movingEntity.setVy(0);
						movingEntity.setGravityVy(0);
					}
				}
			}
			
			if(Math.abs(rect.x-(still.x+still.width)) <= 1){
				//System.out.println("LEFT "+still.toString());
				//moving.setPosition(rect.getX(), moving.getY());
				resolvedX = rect.getX();
			}else if(Math.abs(rect.x+rect.width-still.x) <= 1){
				//System.out.println("RIGHT "+still.toString());
				//moving.setPosition(rect.getX(), moving.getY());
				resolvedX = rect.getX();
			}
			moving.setPosition(resolvedX, resolvedY);
		}
	}
	
	public static void resolveDynamicEntity(DynamicEntity dyn1, Vector2 lastPos1,
			DynamicEntity dyn2, Vector2 lastPos2){
		if(dyn1.getWorldCollider().overlaps(dyn2.getWorldCollider())){
			dyn1.getBaseEntity().flag.collidingEntity.add(dyn2.getBaseEntity());
			dyn2.getBaseEntity().flag.collidingEntity.add(dyn1.getBaseEntity());
			
			if(!dyn1.isOtherCanPass(dyn2)){
				if(!dyn2.isOtherCanPass(dyn1)){
					//TODO: Double collision (double rigid involved)
					
				}else{
					//1 remains in place (rigid involved)
					resolveDynamic(dyn1, dyn2, lastPos2);
				}
			}else{
				if(!dyn2.isOtherCanPass(dyn1)){
					//2 remains in place (rigid involved)
					resolveDynamic(dyn2, dyn1, lastPos1);
				}else{
					//No collision response required
				}
			}
		}
	}
}

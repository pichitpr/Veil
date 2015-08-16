package com.veil.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.veil.game.collider.CollisionResolver;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Player;
import com.veil.game.element.ScriptedEntity;
import com.veil.game.level.GameMap;
import com.veil.game.level.LevelContainer;
import com.veil.game.level.TiledMap;


public class BattleScene implements Screen, LevelContainer{

	final TheGame game;
	private OrthographicCamera camera;
	
	private GameMap map;
	private Player player;
	private List<DynamicEntity> permanentDynList;
	private List<DynamicEntity> pendingSpawnList;
	//private List<DynamicEntity> temporaryDynList;
	
	public BattleScene(TheGame game){
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameConstant.screenW, GameConstant.screenH);
		
		map = new TiledMap();
		player = new Player(this, 1);
		player.setBaseHP(1000);
		player.maxhp = 1000;
		permanentDynList = new ArrayList<DynamicEntity>();
		pendingSpawnList = new ArrayList<DynamicEntity>();
		//permanentDynList.add(new ScriptedEntity(this, new Rectangle(400,70,32,32), 2));
		//temporaryDynList = new ArrayList<DynamicEntity>();
		
		//permanentDynList.add(new ScriptedEntity(this, "Watton"));
		//permanentDynList.add(new ScriptedEntity(this, "Garyoby"));
		//permanentDynList.add(new ScriptedEntity(this, "Lakitu"));
		//permanentDynList.add(new ScriptedEntity(this, "ShellKoopa"));
		//permanentDynList.add(new ScriptedEntity(this, "TomahawkMan"));
		permanentDynList.add(new ScriptedEntity(this, "Batonton"));
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		
		//Draw
		game.batch.begin();
		map.render(game.batch,game.region);
		player.render(game.batch,game.region);
		for(DynamicEntity dyn : permanentDynList){
			dyn.render(game.batch,game.region);
		}
		/*for(DynamicEntity dyn : temporaryDynList){
			dyn.render(game.batch,game.region);
		}*/
		game.font.draw(game.batch, "HP:"+player.getBaseHP()+"/"+player.maxhp, 10, GameConstant.screenH-20);
		game.batch.end();
		
		//=================================================================
		// Update
		//=================================================================
		
		//Update static entities
		map.update(delta);
		
		//Update dynamic entities -- flag used
		player.update(delta);
		for(DynamicEntity dyn : permanentDynList){
			dyn.update(delta);
		}
		/*for(DynamicEntity dyn : temporaryDynList){
			dyn.update(delta);
		}*/
		
		//Flag clear
		player.flag.clear();
		for(DynamicEntity dyn : permanentDynList){
			dyn.flag.clear();
		}
		/*for(DynamicEntity dyn : temporaryDynList){
			dyn.flag.clear();
		}*/
		
		//Response to static environment -- flag set
		getStaticMap().resolveEnvironmentCollisionFor(player, player.getLastPos());
		for(DynamicEntity dyn : permanentDynList){
			if(!dyn.projectile)
				getStaticMap().resolveEnvironmentCollisionFor(dyn, dyn.getLastPos());
		}
		/*for(DynamicEntity dyn2 : temporaryDynList){
			if(!dyn2.projectile)
				getStaticMap().resolveEnvironmentCollisionFor(dyn2, dyn2.getLastPos());
		}*/
		
		//Response to dynamic collider -- flag (colliding entity) set
		for(DynamicEntity dyn : permanentDynList){
			CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
					player, player.getLastPos());
		}
		if(permanentDynList.size() > 1){
			DynamicEntity dyn,dyn2;
			for(int i=0; i<permanentDynList.size()-1; i++){
				dyn = permanentDynList.get(i);
				for(int j=i+1; j<permanentDynList.size(); j++){
					dyn2 = permanentDynList.get(j);
					CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
							dyn2, dyn2.getLastPos());
				}
			}
		}
		
		/*for(DynamicEntity dyn : permanentDynList){
			CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
					player, player.getLastPos());
			for(DynamicEntity dyn2 : temporaryDynList){
				CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
						dyn2, dyn2.getLastPos());
			}
		}
		for(DynamicEntity dyn2 : temporaryDynList){
			CollisionResolver.resolveDynamicEntity(dyn2, dyn2.getLastPos(), 
					player, player.getLastPos());
		}*/
		
		//Handle collision event -- flag used
		player.handleCollisionEvent();
		for(DynamicEntity dyn : permanentDynList){
			dyn.handleCollisionEvent();
		}
		/*for(DynamicEntity dyn : temporaryDynList){
			dyn.handleCollisionEvent();
		}*/
		
		//Propagate despawning to children
		for(int i=0; i<permanentDynList.size(); i++){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				for(DynamicEntity dyn : permanentDynList.get(i).children){
					dyn.despawn();
				}
			}
		}
		/*for(int i=0; i<temporaryDynList.size(); i++){
			if(temporaryDynList.get(i).shouldDespawn()){
				for(DynamicEntity dyn : temporaryDynList.get(i).children){
					dyn.despawn();
				}
			}
		}*/
		
		//Handle OnDespawn event
		for(int i=0; i<permanentDynList.size(); i++){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				permanentDynList.get(i).onDespawn(delta);
			}
		}
		/*for(int i=0; i<temporaryDynList.size(); i++){
			if(temporaryDynList.get(i).shouldDespawn()){
				temporaryDynList.get(i).onDespawn(delta);
			}
		}*/
		
		//Clear "despawned" entity
		for(int i=permanentDynList.size()-1; i>=0; i--){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				permanentDynList.remove(i);
			}
		}
		/*for(int i=temporaryDynList.size()-1; i>=0; i--){
			if(temporaryDynList.get(i).shouldDespawn()){
				temporaryDynList.remove(i);
			}
		}*/
		
		for(DynamicEntity dyn : pendingSpawnList){
			permanentDynList.add(dyn);
		}
		pendingSpawnList.clear();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	//=======================================================
	
	@Override
	public GameMap getStaticMap() {
		return map;
	}
	
	@Override
	public float getGravity(){
		return GameConstant.gravity;
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public List<DynamicEntity> getPermanentDynamicEntity() {
		return permanentDynList;
	}

	@Override
	public void pendingSpawn(DynamicEntity dyn) {
		pendingSpawnList.add(dyn);
	}

	/*
	@Override
	public List<DynamicEntity> getTemporaryDynamicEntity() {
		return temporaryDynList;
	}
	*/

}

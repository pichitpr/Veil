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
	private DynamicEntity enemy;
	private List<DynamicEntity> permanentDynList;
	private List<DynamicEntity> pendingSpawnList;
	private List<DynamicEntity> temporaryDynList;
	
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
		temporaryDynList = new ArrayList<DynamicEntity>();
		
		permanentDynList.add(new ScriptedEntity(this, "agent0"));
		//permanentDynList.add(new ScriptedEntity(this, "Lakitu"));
		//permanentDynList.add(new ScriptedEntity(this, "ShellKoopa"));
		//permanentDynList.add(new ScriptedEntity(this, "TomahawkMan"));
		
		//permanentDynList.add(new ScriptedEntity(this, "Batonton"));
		//permanentDynList.add(new ScriptedEntity(this, "Biree"));
		//permanentDynList.add(new ScriptedEntity(this, "BoulderSpawner"));
		//permanentDynList.add(new ScriptedEntity(this, "DocronHatcher"));
		//permanentDynList.add(new ScriptedEntity(this, "Dompan"));
		//permanentDynList.add(new ScriptedEntity(this, "Gachappon"));
		//permanentDynList.add(new ScriptedEntity(this, "Garyoby"));
		//permanentDynList.add(new ScriptedEntity(this, "Gyotot"));
		//permanentDynList.add(new ScriptedEntity(this, "Haehaey"));
		//permanentDynList.add(new ScriptedEntity(this, "Helipon"));
		//permanentDynList.add(new ScriptedEntity(this, "Jumbig"));
		//permanentDynList.add(new ScriptedEntity(this, "Mantan"));
		//permanentDynList.add(new ScriptedEntity(this, "MetallRun"));
		//permanentDynList.add(new ScriptedEntity(this, "MetallJump"));
		//permanentDynList.add(new ScriptedEntity(this, "MetallSwim"));
		//permanentDynList.add(new ScriptedEntity(this, "MetallSpin"));
		//permanentDynList.add(new ScriptedEntity(this, "Monoroader"));
		//permanentDynList.add(new ScriptedEntity(this, "Mummira"));
		//permanentDynList.add(new ScriptedEntity(this, "Pakatto"));
		//permanentDynList.add(new ScriptedEntity(this, "Puyoyon"));
		//permanentDynList.add(new ScriptedEntity(this, "Ratton"));
		//permanentDynList.add(new ScriptedEntity(this, "Ringring"));
		//permanentDynList.add(new ScriptedEntity(this, "Sasoreenu"));
		//permanentDynList.add(new ScriptedEntity(this, "Seamine"));
		//permanentDynList.add(new ScriptedEntity(this, "SkeletonJoe"));
		//permanentDynList.add(new ScriptedEntity(this, "BallCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "Togehero"));
		//permanentDynList.add(new ScriptedEntity(this, "TotemPolem"));
		//permanentDynList.add(new ScriptedEntity(this, "UpDown"));
		//permanentDynList.add(new ScriptedEntity(this, "WallBlaster"));
		//permanentDynList.add(new ScriptedEntity(this, "Watton"));
		//permanentDynList.add(new ScriptedEntity(this, "Moby"));
		//permanentDynList.add(new ScriptedEntity(this, "BrightMan"));
		//permanentDynList.add(new ScriptedEntity(this, "ToadMan"));
		//permanentDynList.add(new ScriptedEntity(this, "DrillMan"));
		//permanentDynList.add(new ScriptedEntity(this, "PharaohMan"));
		//permanentDynList.add(new ScriptedEntity(this, "DustMan"));
		//permanentDynList.add(new ScriptedEntity(this, "DiveMan"));
		//permanentDynList.add(new ScriptedEntity(this, "SkullMan"));
		//permanentDynList.add(new ScriptedEntity(this, "Roach01"));
		//permanentDynList.add(new ScriptedEntity(this, "Roach02"));
		//permanentDynList.add(new ScriptedEntity(this, "CossackMachine"));
		//permanentDynList.add(new ScriptedEntity(this, "MetallDaddy"));
		//permanentDynList.add(new ScriptedEntity(this, "Wily04_01"));
		//permanentDynList.add(new ScriptedEntity(this, "Wily04_02"));
		//permanentDynList.add(new ScriptedEntity(this, "Wily04_03"));
		
		//permanentDynList.add(new ScriptedEntity(this, "Crawler01"));
		//permanentDynList.add(new ScriptedEntity(this, "Flyer01Spawner"));
		//permanentDynList.add(new ScriptedEntity(this, "Swarm01"));
		//permanentDynList.add(new ScriptedEntity(this, "Swooper01"));
		//permanentDynList.add(new ScriptedEntity(this, "Hopper01"));
		//permanentDynList.add(new ScriptedEntity(this, "Hopper02"));
		//permanentDynList.add(new ScriptedEntity(this, "Floater01"));
		//permanentDynList.add(new ScriptedEntity(this, "Floater02"));
		//permanentDynList.add(new ScriptedEntity(this, "Skree"));
		//permanentDynList.add(new ScriptedEntity(this, "Multiviola"));
		//permanentDynList.add(new ScriptedEntity(this, "Waver"));
		//permanentDynList.add(new ScriptedEntity(this, "Squeept"));
		//permanentDynList.add(new ScriptedEntity(this, "LavaDragon"));
		//permanentDynList.add(new ScriptedEntity(this, "RinkaSpawner"));
		//permanentDynList.add(new ScriptedEntity(this, "MetroidCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "Metroid"));
		//permanentDynList.add(new ScriptedEntity(this, "Kraid"));
		//permanentDynList.add(new ScriptedEntity(this, "Ridley"));
		
		//permanentDynList.add(new ScriptedEntity(this, "Soldier"));
		//permanentDynList.add(new ScriptedEntity(this, "Sniper"));
		//permanentDynList.add(new ScriptedEntity(this, "SniperCrawl"));
		//permanentDynList.add(new ScriptedEntity(this, "BombThrower"));
		//permanentDynList.add(new ScriptedEntity(this, "HiddenCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "ShootingSoldier"));
		//permanentDynList.add(new ScriptedEntity(this, "RLSoldier"));
		//permanentDynList.add(new ScriptedEntity(this, "RLSniperStand"));
		//permanentDynList.add(new ScriptedEntity(this, "RLSniperMove"));
		//permanentDynList.add(new ScriptedEntity(this, "RLTank"));
		//permanentDynList.add(new ScriptedEntity(this, "RLCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "GroundBarrel"));
		//permanentDynList.add(new ScriptedEntity(this, "HiddenSniper"));
		//permanentDynList.add(new ScriptedEntity(this, "Sniper3Shot"));
		//permanentDynList.add(new ScriptedEntity(this, "SplashCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "SpiderTank"));
		//permanentDynList.add(new ScriptedEntity(this, "AlienEggSpawner"));
		//permanentDynList.add(new ScriptedEntity(this, "WingedSoldier"));
		//permanentDynList.add(new ScriptedEntity(this, "AlienEggHoming"));
		//permanentDynList.add(new ScriptedEntity(this, "RailCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "HiddenCannon02"));
		//permanentDynList.add(new ScriptedEntity(this, "HiddenDomeCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "FlyingSniper"));
		//permanentDynList.add(new ScriptedEntity(this, "GroundBarrel02"));
		//permanentDynList.add(new ScriptedEntity(this, "SkullUFO"));
		//permanentDynList.add(new ScriptedEntity(this, "RLSpider"));
		//permanentDynList.add(new ScriptedEntity(this, "RLSpiderShooter"));
		//permanentDynList.add(new ScriptedEntity(this, "BigSpiky"));
		//permanentDynList.add(new ScriptedEntity(this, "Hydra"));
		//permanentDynList.add(new ScriptedEntity(this, "TwinMouth"));
		//permanentDynList.add(new ScriptedEntity(this, "AlienMinionSpawner"));
		//permanentDynList.add(new ScriptedEntity(this, "ShootingMouth"));
		//permanentDynList.add(new ScriptedEntity(this, "AlienCannon"));
		//permanentDynList.add(new ScriptedEntity(this, "RedFalcon"));
		
		//permanentDynList.add(new ScriptedEntity(this, "BombMan"));
		//permanentDynList.add(new ScriptedEntity(this, "GutsMan"));
		//permanentDynList.add(new ScriptedEntity(this, "FireMan"));
		//permanentDynList.add(new ScriptedEntity(this, "ElecMan"));
		//permanentDynList.add(new ScriptedEntity(this, "IceMan"));
		//permanentDynList.add(new ScriptedEntity(this, "CopyRobot"));
		//permanentDynList.add(new ScriptedEntity(this, "CWU01P"));
		//permanentDynList.add(new ScriptedEntity(this, "Wily01_01"));
		
		//permanentDynList.add(new ScriptedEntity(this, "BlackKnight01"));
		//permanentDynList.add(new ScriptedEntity(this, "KingKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "PlagueKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "TreasureKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "MoleKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "TinkerKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "PolarKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "PropellerKnight"));
		//permanentDynList.add(new ScriptedEntity(this, "BlackKnight03"));
		//permanentDynList.add(new ScriptedEntity(this, "Baz"));
		//permanentDynList.add(new ScriptedEntity(this, "PhantomStriker"));
		//permanentDynList.add(new ScriptedEntity(this, "MrHat"));
		
		enemy = permanentDynList.get(0);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		
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
		for(DynamicEntity dyn : temporaryDynList){
			dyn.update(delta);
		}
		
		//Flag clear
		player.flag.clear();
		for(DynamicEntity dyn : permanentDynList){
			dyn.flag.clear();
		}
		for(DynamicEntity dyn : temporaryDynList){
			dyn.flag.clear();
		}
		
		//Response to static environment -- flag set
		getStaticMap().resolveEnvironmentCollisionFor(player, player.getLastPos());
		for(DynamicEntity dyn : permanentDynList){
			if(!dyn.projectile)
				getStaticMap().resolveEnvironmentCollisionFor(dyn, dyn.getLastPos());
		}
		for(DynamicEntity dyn2 : temporaryDynList){
			if(!dyn2.projectile)
				getStaticMap().resolveEnvironmentCollisionFor(dyn2, dyn2.getLastPos());
		}
		
		//Response to dynamic collider -- flag (colliding entity) set
		/*
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
		*/
		for(DynamicEntity dyn : permanentDynList){
			CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
					player, player.getLastPos());
		}
		for(DynamicEntity dyn2 : temporaryDynList){
			CollisionResolver.resolveDynamicEntity(dyn2, dyn2.getLastPos(), 
					player, player.getLastPos());
		}
		for(int i=0; i<permanentDynList.size()-1; i++){
			DynamicEntity dyn = permanentDynList.get(i);
			for(int j=i+1; j<permanentDynList.size(); j++){
				DynamicEntity dyn2 = permanentDynList.get(j);
				CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
						dyn2, dyn2.getLastPos());
			}
		}
		for(int i=0; i<temporaryDynList.size()-1; i++){
			DynamicEntity dyn = temporaryDynList.get(i);
			for(int j=i+1; j<temporaryDynList.size(); j++){
				DynamicEntity dyn2 = temporaryDynList.get(j);
				CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
						dyn2, dyn2.getLastPos());
			}
		}
		for(int i=0; i<permanentDynList.size(); i++){
			DynamicEntity dyn = permanentDynList.get(i);
			for(int j=0; j<temporaryDynList.size(); j++){
				DynamicEntity dyn2 = temporaryDynList.get(j);
				CollisionResolver.resolveDynamicEntity(dyn, dyn.getLastPos(), 
						dyn2, dyn2.getLastPos());
			}
		}
		
		//Handle collision event -- flag used
		player.handleCollisionEvent();
		for(DynamicEntity dyn : permanentDynList){
			dyn.handleCollisionEvent();
		}
		for(DynamicEntity dyn : temporaryDynList){
			dyn.handleCollisionEvent();
		}
		
		//===========================================================
		// Render
		//===========================================================
		game.batch.begin();
		map.render(game.batch,game.region);
		player.render(game.batch,game.region);
		for(DynamicEntity dyn : permanentDynList){
			dyn.render(game.batch,game.region);
		}
		for(DynamicEntity dyn : temporaryDynList){
			dyn.render(game.batch,game.region);
		}
		game.font.draw(game.batch, "HP:"+player.getBaseHP()+"/"+player.maxhp, 10, GameConstant.screenH-20);
		game.font.draw(game.batch, "HP:"+enemy.getBaseHP(), GameConstant.screenW/2, GameConstant.screenH-20);
		game.batch.end();
		
		//===========================================================
		// Despawn
		//===========================================================
		
		//Propagate despawning to children
		for(int i=0; i<permanentDynList.size(); i++){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				for(DynamicEntity dyn : permanentDynList.get(i).children){
					dyn.despawn();
				}
			}
		}
		for(int i=0; i<temporaryDynList.size(); i++){
			if(temporaryDynList.get(i).shouldBeRemovedFromWorld()){
				for(DynamicEntity dyn : temporaryDynList.get(i).children){
					dyn.despawn();
				}
			}
		}
		
		//Handle OnDespawn event
		for(int i=0; i<permanentDynList.size(); i++){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				permanentDynList.get(i).onDespawn(delta);
			}
		}
		for(int i=0; i<temporaryDynList.size(); i++){
			if(temporaryDynList.get(i).shouldBeRemovedFromWorld()){
				temporaryDynList.get(i).onDespawn(delta);
			}
		}
		
		//Clear "despawned" entity
		for(int i=permanentDynList.size()-1; i>=0; i--){
			if(permanentDynList.get(i).shouldBeRemovedFromWorld()){
				permanentDynList.remove(i);
			}
		}
		for(int i=temporaryDynList.size()-1; i>=0; i--){
			if(temporaryDynList.get(i).shouldBeRemovedFromWorld()){
				temporaryDynList.remove(i);
			}
		}
		
		for(DynamicEntity dyn : pendingSpawnList){
			temporaryDynList.add(dyn);
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

	@Override
	public boolean canHandleMoreEntity() {
		return temporaryDynList.size() + pendingSpawnList.size() <= GameConstant.maxObjectPool;
	}

	@Override
	public List<DynamicEntity> getTemporaryDynamicEntity() {
		return temporaryDynList;
	}

}

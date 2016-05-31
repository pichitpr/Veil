package com.veil.game;

import com.badlogic.gdx.files.FileHandle;
import com.veil.platforminterface.PlatformUtility;


public class GameConstant {

	public static final int screenW = 640;
	public static final int screenH = 64*9;
	
	public static final int tileSizeX = 32;
	public static final int tileSizeY = 32;
	
	public static final float speed = 5;
	
	/**
	 * This flag only specify how player control the character
	 */
	public static final boolean isPlaformer = true;
	
	/**
	 * Specify downward force applied to every dynamic 
	 */
	public static final float gravity = 1.5f;
	
	public static final float jumpSpeed = 2.7f;
	public static final int jumpCounter = 7;
	
	/**
	 * Auto despawning time (in frame) after the dynamic leaves the screen
	 */
	public static final int autoDespawnTime = 100;
	
	/**
	 * Max number of objects in the pool that does not allow Spawn() to spawn more
	 */
	public static final int maxObjectPool = 20;
	
	/**
	 * A directory containing all agent scripts to be used
	 */
	public static final FileHandle agentDatabaseDir = PlatformUtility.fileOperator.getStorageRoot().child("ScriptGen");
	
	//Task specific variable
	public static final boolean profilingMode = true;
	public static final boolean useAI = true;
	public static final boolean timeStepping = true;
	public static final boolean debugDrawing = true;
}

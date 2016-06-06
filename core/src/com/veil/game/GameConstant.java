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
	 * A directory containing all agent scripts to be used.
	 */
	public static final FileHandle agentDatabaseDir = PlatformUtility.fileOperator.getStorageRoot().child("ScriptGen");
	
	//==========================================
	//Task specific variable
	//==========================================
	/**
	 * Turn on/off profiling mode. In profiling mode, player have to rush through all enemies in subdirectory "Rush"
	 * of agent database directory. The flag must be set before BattleScene creation.
	 */
	public static final boolean profilingMode = true;
	/**
	 * Turn on/off range profiling mode (also requires profiling mode ON). In this mode, player have to avoid enemies' attack
	 * with a single jump. Each battle session end after the first enemy bullet leaves screen. Enemies are taken from
	 * subdirectory "RangeRush" of agent database directory. Every enemies' name must end with "_X" where X is a number
	 * indicating its attack speed. The flag must be set before BattleScene creation.
	 */
	public static final boolean rangeProfiling = true;
	public static final int repeat = 3;
	/**
	 * Profile saving directory. Range profile will also be saved here under filename "range_profile.txt"
	 */
	public static final FileHandle profileDir = PlatformUtility.fileOperator.getStorageRoot().child("Profile");
	
	public static final boolean useAI = false;
	public static final boolean timeStepping = false;
	public static final boolean debugDrawing = true;
}

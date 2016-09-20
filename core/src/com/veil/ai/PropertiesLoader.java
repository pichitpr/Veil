package com.veil.ai;

import java.io.FileInputStream;
import java.util.Properties;

import com.badlogic.gdx.files.FileHandle;

public class PropertiesLoader {

	public static void loadProperties(FileHandle fh) throws Exception{
		FileInputStream fs = new FileInputStream(fh.file());
		Properties prop = new Properties();
		prop.load(fs);
		fs.close();
		if(GameAI.instance instanceof GameAI_v5){
			GameAI_v5 aiV5 = (GameAI_v5)GameAI.instance;
			aiV5.buttonChangeDelay = Integer.parseInt(prop.getProperty("buttonChangeDelay")); 
			aiV5.simulationFrame = Integer.parseInt(prop.getProperty("simulationFrame")); 
			aiV5.yDiffShootingMargin = Float.parseFloat(prop.getProperty("yDiffShootingMargin")); 
			aiV5.shootingDelay = Integer.parseInt(prop.getProperty("shootingDelay")); 
			aiV5.retainDurationAfterRunTowardEnemy = Integer.parseInt(prop.getProperty("retainDurationAfterRunTowardEnemy")); 
		}
	}
}

package com.veil.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.veil.game.TheGame;
import com.veil.platforminterface.PlatformUtility;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Veil";
		config.width = 640;
		config.height = 64*9;
		new LwjglApplication(new TheGame(), config);
		PlatformUtility.fileOperator = new DesktopFileOperator();
	}
}

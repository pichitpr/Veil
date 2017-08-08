package com.veil.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.veil.platforminterface.FileOperator;

public class DesktopFileOperator implements FileOperator{

	//Project root, use getCurrentWkDir() if export the game as jar
	private final FileHandle root = Gdx.files.absolute("C:\\eclipse luna\\work\\Veil");
	//private final FileHandle root = Gdx.files.absolute(getCurrentWkDir());
	
	public String getCurrentWkDir(){
		String path = DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(1, path.lastIndexOf('/'));
		path = path.replaceAll("%20", " ");
		return path;
	}
	
	@Override
	public FileHandle getStorageRoot() {
		return root;
	}

}

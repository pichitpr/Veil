package com.veil.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.veil.platforminterface.FileOperator;

public class DesktopFileOperator implements FileOperator{

	private final FileHandle root = Gdx.files.absolute("G:\\libgdx\\Veil");
	
	@Override
	public FileHandle getStorageRoot() {
		return root;
	}

}

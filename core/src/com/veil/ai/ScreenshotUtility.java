package com.veil.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

public class ScreenshotUtility {
	
	public static void takeScreenshot(FileHandle dir, String filename){
		byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
		BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
		
		PixmapIO.writePNG(dir.child(filename+".png"), pixmap);
		pixmap.dispose();
	}
}

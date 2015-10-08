package com.veil.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.veil.adl.AgentDatabase;

public class TheGame extends Game {
	public SpriteBatch batch;
	public AtlasRegion[] region;
    public BitmapFont font;
    
    public void create() {
    	if(!AgentDatabase.setup()){
    		System.exit(0);
    	}
    	System.out.println("Data loaded successful!!");
    	
    	MathUtils.random = new RandomXS128();
    	
    	TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("panel.atlas"));
    	region = new AtlasRegion[7];
    	region[1] = atlas.findRegion("frame_a"); //Player
    	region[2] = atlas.findRegion("frame_c"); //Tile 1
    	region[3] = atlas.findRegion("frame_d"); //Tile 2
		region[4] = atlas.findRegion("frame_f"); //Enemy 1
		region[5] = atlas.findRegion("frame_b"); //Enemy 2
		region[6] = atlas.findRegion("frame_e"); //Bullet 1
    	

        batch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        font = new BitmapFont();
        font.setScale(2);
        
        this.setScreen(new BattleScene(this));
    }

    public void render() {
        super.render(); //important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

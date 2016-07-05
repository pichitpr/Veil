package com.veil.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.veil.adl.AgentDatabase;
import com.veil.ai.Controller;

public class TheGame extends Game {
	public ShapeRenderer shapeBatch;
	public SpriteBatch batch;
	public AtlasRegion[] region;
    public BitmapFont font;
    
    public void create() {
    	Controller.instance.setup();
    	if(!AgentDatabase.setup()){
    		System.exit(0);
    	}
    	System.out.println("Data loaded successful!!");
    	
    	MathUtils.random = new RandomXS128(1000);
    	
    	TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("panel.atlas"));
    	region = new AtlasRegion[7];
    	region[1] = atlas.findRegion("frame_a"); //Player
    	region[2] = atlas.findRegion("frame_c"); //Tile 1
    	region[3] = atlas.findRegion("frame_d"); //Tile 2
		region[4] = atlas.findRegion("frame_f"); //Enemy 1
		region[5] = atlas.findRegion("frame_b"); //Enemy 2
		region[6] = atlas.findRegion("frame_e"); //Bullet 1
    	
		shapeBatch = new ShapeRenderer();
        batch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        font = new BitmapFont();
        
        
        this.setScreen(new InfoScene(this,0));
    }

    public void render() {
        super.render(); //important!
    }

    public void dispose() {
    	shapeBatch.dispose();
        batch.dispose();
        font.dispose();
    }
    
    private int sequence = 0;
    
    public void nextScene(){
    	switch(sequence){
    	case 0: case 2: case 4:
    		this.setScreen(new InfoScene(this,sequence));
    		break;
    	case 1: 
    		GameConstant.profilingMode = false;
    		GameConstant.rangeProfiling = false;
    		this.setScreen(new BattleScene(this));
    		break;
    	case 3:
    		GameConstant.profilingMode = true;
    		GameConstant.rangeProfiling = true;
    		this.setScreen(new BattleScene(this));
    		break;
    	case 6:
    		GameConstant.profilingMode = true;
    		GameConstant.rangeProfiling = false;
    		this.setScreen(new BattleScene(this));
    		break;
    	case 7:
    		Gdx.app.exit();
    		return;
    	}
    	sequence++;
    }
}

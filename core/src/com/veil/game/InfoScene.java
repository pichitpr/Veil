package com.veil.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.veil.ai.Controller;

public class InfoScene implements Screen{

	final TheGame game;
	private OrthographicCamera camera;
	private Texture tx = null;
	
	private int sceneSequence;
	
	public InfoScene(TheGame game, int sequence){
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameConstant.screenW, GameConstant.screenH);
		sceneSequence = sequence;
		if(sequence == 0 && Controller.instance.isUsingController()){
			tx = new Texture(Gdx.files.internal("controller.png"));
		}
	}
	
	@Override
	public void render(float delta) {
		Controller.instance.preUpdateInfoScene();
		if(Controller.instance.pause){
			game.nextScene();
			return;
		}
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
		game.font.setScale(1f);
		
		switch(sceneSequence){
		case 0:
			drawMultiline(new String[]{
					"In the first session, you'll have to defeat an enemy. An enemy can be beaten by bullet",
					"Controlling scheme is shown below."
			}, 20, GameConstant.screenH-20, 30, game.batch, game.font);
			/*
			game.font.draw(game.batch, 
					"This experiment collects player's action overtime to be used in game AI evaluation", 
					20, GameConstant.screenH-20);
			game.font.draw(game.batch, 
					"In the first session, you'll have to defeat an enemy. An enemy can be beaten by bullet", 
					20, GameConstant.screenH-50);
			game.font.draw(game.batch, 
					"Controlling scheme is shown below.", 
					20, GameConstant.screenH-80);
					*/
			if(Controller.instance.isUsingController()){
				game.batch.draw(tx, (GameConstant.screenW-tx.getWidth())/2, (GameConstant.screenH-tx.getHeight())/2, 
						tx.getWidth(), tx.getHeight());
			}else{
				drawMultiline(new String[]{
						"[Left][Right] Move player",
						"[Z] Jump",
						"[X] Shoot",
						"[Esc] Skip battle"
				}, 20, GameConstant.screenH/2+60, 30, game.batch, game.font);
				/*
				game.font.draw(game.batch, "[Left][Right] Move player", 100, GameConstant.screenH/2+60);
				game.font.draw(game.batch, "[Z] Jump", 100, GameConstant.screenH/2+20);
				game.font.draw(game.batch, "[X] Shoot", 100, GameConstant.screenH/2-20);
				game.font.draw(game.batch, "[Esc] Skip battle", 100, GameConstant.screenH/2-60);
				*/
			}
			game.font.draw(game.batch, "You can shoot AFTER a battle has been going on for 2 seconds.", 20, 120);
			game.font.draw(game.batch, "Press \"Skip battle\" to continue. (May need to press twice)", 20, 80);
			break;
		case 2:
			drawMultiline(new String[]{
					"In the next session, you'll have to dodge enemy's attack by jumping.",
					"Every enemy will shoot a bullet at a random speed in a random interval",
					"You can jump only ONCE for each battle. After you either successfully dodge",
					"or fail, the battle is end immediately."
			}, 20, GameConstant.screenH-20, 30, game.batch, game.font);
			/*
			game.font.draw(game.batch, 
					"In the next session, you'll have to dodge enemy's attack by jumping.", 
					20, GameConstant.screenH-20);
			game.font.draw(game.batch, 
					"Every enemy will shoot a bullet at a random speed in a random interval", 
					20, GameConstant.screenH-50);
			game.font.draw(game.batch, 
					"You can jump only ONCE for each battle. After you either successfully dodge", 
					20, GameConstant.screenH-80);
			game.font.draw(game.batch, 
					"or fail, you have to SKIP the current battle MANUALLY.", 
					20, GameConstant.screenH-110);
					*/
			game.font.draw(game.batch, "Press \"Skip battle\" to continue. (May need to press twice)", 20, 80);
			break;
		case 4:
			drawMultiline(new String[]{
					"In the final session, you'll have to fight against random enemies. Do your best!", 
					"However, if the enemy is unbeatable for any reason, you can just skip the battle.",
					"The game will be automatically closed when this session end"
			}, 20, GameConstant.screenH-20, 30, game.batch, game.font);
			/*
			game.font.draw(game.batch, 
					"In the final session, you'll have to fight against random enemies. Do your best!", 
					20, GameConstant.screenH-20);
			game.font.draw(game.batch, 
					"However, if the enemy is unbeatable, you can just skip the battle", 
					20, GameConstant.screenH-50);
					*/
			game.font.draw(game.batch, "Press \"Skip battle\" to continue. (May need to press twice)", 20, 80);
			break;
		}
		
		game.batch.end();
	}
	
	private static void drawMultiline(String[] texts, int startX, int startY, int gap, SpriteBatch batch, BitmapFont font){
		int y = startY;
		for(String text : texts){
			font.draw(batch, text, startX, y);
			y -= gap;
		}
	}
	
	//=======================================
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
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
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		if(tx != null){
			tx.dispose();
		}
	}

}

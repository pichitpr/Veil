package com.veil.game.level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.GameConstant;
import com.veil.game.collider.CollisionResolver;
import com.veil.game.element.DynamicEntity;

public class TiledMap implements GameMap {

	private int[][] tile = new int[][]{
			new int[]{1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			//new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 1,1,1,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			//new int[]{1,0,0,0,0, 0,0,0,1,1, 1,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			//new int[]{1,1,1,1,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,1},
			new int[]{1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1},
	};
	
	public TiledMap(){
		reverseTileY();
	}
	
	private void reverseTileY(){
		int i=0,j=tile.length-1;
		int[] tmp;
		while(i<=j){
			tmp = tile[i];
			tile[i] = tile[j];
			tile[j] = tmp;
			i++;
			j--;
		}
	}
	
	public int[][] getTiles(){
		return tile;
	}
	
	public void render(SpriteBatch batch,AtlasRegion[] atlas){
		for(int y=0; y<tile.length; y++){
			for(int x=0; x<tile[0].length; x++){
				if(tile[y][x] == 1){
					batch.draw(atlas[3], x*GameConstant.tileSizeX, y*GameConstant.tileSizeY, 
						GameConstant.tileSizeX, GameConstant.tileSizeY);
				}else if(tile[y][x] == -1){
					batch.draw(atlas[1], x*GameConstant.tileSizeX, y*GameConstant.tileSizeY, 
							GameConstant.tileSizeX, GameConstant.tileSizeY);
				}
			}
		}
	}

	@Override
	public void resolveEnvironmentCollisionFor(DynamicEntity dyn, Vector2 lastPos) {
		/*CollisionResolver.resolveTiledBased(this, dyn, 
				dyn.getCollider().x-lastPos.x,
				dyn.getCollider().y-lastPos.y);*/
		CollisionResolver.resolveTiledBased(this, dyn, lastPos);
	}

	public boolean outOfMap(int sx,int sy,int ex,int ey){
		return (sx < 0 || sx >= tile[0].length || sy < 0 || sy >= tile.length ||
				ex < 0 || ex >= tile[0].length || ey < 0 || ey >= tile.length);
	}
	
	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getMapSize() {
		return new int[]{640,64*9};
	}
}

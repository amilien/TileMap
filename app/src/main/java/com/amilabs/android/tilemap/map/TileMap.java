package com.amilabs.android.tilemap.map;

import com.amilabs.android.tilemap.utils.Constants;

/**
 * TileMap is representing a map.
 * Different map implementation can be done here in initMap().
 * @author Mike
 *
 */
public class TileMap implements Constants {

	private static TileMap mInstance;
    // 2D-array of tiles representing map
    private Tile[][] mTiles;
	
	private TileMap() {
        mTiles = new Tile[TILES_NUMBER_Y][TILES_NUMBER_X];
	}
	
	public static TileMap getInstance() {
		if (mInstance == null)
			mInstance = new TileMap();
		return mInstance;
	}
	
	public void initMap() {
        for (int i = 0; i < TILES_NUMBER_Y; i++)
            for (int j = 0; j < TILES_NUMBER_X; j++)
                mTiles[i][j] = new Tile(TILE_URL_1 + (TILE_START_ID_X + j) + "/" + 
                						(TILE_START_ID_Y + i) + TILE_URL_2, 
                						i * TILES_NUMBER_X + j + 1, j, i);
	}
	
	public Tile[][] getMap() {
		return mTiles;
	}
}

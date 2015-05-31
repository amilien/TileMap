package com.amilabs.android.tilemap.utils;

import android.os.Environment;

/**
 * Basic constants.
 * Here you can change the map size, tiles starting positions.
 * @author Mike
 *
 */
public interface Constants {

	public static final int TILES_NUMBER_X = 100;
	public static final int TILES_NUMBER_Y = 100;
    public static final int TILE_WIDTH = 256;
    public static final int TILE_HEIGHT = 256;
    public static final int TILE_START_ID_X = 33198;
    public static final int TILE_START_ID_Y = 22539;
    public static final String TILE_URL_1 = "http://b.tile.opencyclemap.org/cycle/16/";
    public static final String TILE_URL_2 = ".png";
    public static final String CACHE_STORAGE_PATH = Environment.getExternalStorageDirectory() + 
    		"/Android/data/com.amilabs.android.tilemap/";
    public static final int CACHE_STORAGE_SIZE = TILES_NUMBER_X * TILES_NUMBER_Y / 10;
}

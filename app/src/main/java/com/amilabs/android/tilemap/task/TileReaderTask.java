package com.amilabs.android.tilemap.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.amilabs.android.tilemap.cache.Cache;
import com.amilabs.android.tilemap.map.Tile;

/**
 * Decodes storage cached bitmap.
 * @author Mike
 */
public class TileReaderTask extends Thread {

    private Tile mTile;
    private String mPath;
    private Cache<Bitmap> mRAMCache;

    public TileReaderTask(Tile tile, String path, Cache<Bitmap> RAMCache) {
        mTile = tile;
        mPath = path;
        mRAMCache = RAMCache;
    }

    @Override
    public void run() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(mPath, options);
        // set decoded bitmap from Disk to RAM cache
        mRAMCache.set(mTile, bitmap);
    }
}

package com.amilabs.android.tilemap.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import com.amilabs.android.tilemap.cache.Cache;
import com.amilabs.android.tilemap.map.Tile;
import com.amilabs.android.tilemap.utils.Constants;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Downloads tile and decodes into bitmap.
 * @author Mike
 */
public class TileDownloaderTask extends Thread implements Constants {

    private static final String TAG = "TileDownloaderTask";

    private Context mContext;
    private Tile mTile;
    private String mOutputPath;
    private Cache<Bitmap> mRAMCache;
    private Cache<String> mDiskCache;

    public TileDownloaderTask(Context context, Tile tile, Cache<Bitmap> RAMCache,
                              Cache<String> DiskCache) {
        mContext = context;
        mTile = tile;
        mRAMCache = RAMCache;
        mDiskCache = DiskCache;
        mOutputPath = CACHE_STORAGE_PATH + mTile.getIndexX() + "_" + mTile.getIndexY() + ".png";

    }

    private byte[] downloadTile() {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        OutputStream outFile = null;
        byte[] bytes = null;
        try {
            URL url = new URL(mTile.getUrl());
            URLConnection con = url.openConnection();
            con.connect();
            input = new BufferedInputStream(url.openStream());
            output = new ByteArrayOutputStream();
            outFile = new FileOutputStream(mOutputPath);
            byte data[] = new byte[4096];
            int count = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                outFile.write(data, 0, count);
            }
            bytes = output.toByteArray();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException in downloadTile.");
        } catch (IOException e) {
            Log.e(TAG, "IOException in downloadTile. ");
        } finally {
            if (outFile != null) try { outFile.close(); } catch(IOException e) {}
            if (output != null) try { output.close(); } catch(IOException e) {}
            if (input != null) try { input.close(); } catch(IOException e) {}
        }
        return bytes;
    }

    private Bitmap decodeBitmap(byte[] bytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        DisplayMetrics metrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
        options.inScreenDensity = metrics.densityDpi;
        options.inTargetDensity =  metrics.densityDpi;
        options.inDensity = metrics.densityDpi;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    @Override
    public void run() {
        mTile.setDownloading(true);
        byte[] bytes = downloadTile();
        Bitmap bmp = decodeBitmap(bytes);
        mRAMCache.set(mTile, bmp);
        mDiskCache.set(mTile, mOutputPath);
        mTile.setDownloading(false);
    }
}

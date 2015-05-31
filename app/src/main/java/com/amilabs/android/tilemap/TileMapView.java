package com.amilabs.android.tilemap;

import java.io.File;

import com.amilabs.android.tilemap.cache.Cache;
import com.amilabs.android.tilemap.cache.LRUCache;
import com.amilabs.android.tilemap.map.Tile;
import com.amilabs.android.tilemap.map.TileMap;
import com.amilabs.android.tilemap.task.TileDownloaderTask;
import com.amilabs.android.tilemap.task.TileReaderTask;
import com.amilabs.android.tilemap.utils.Constants;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * View displaying the map.
 * Contains:
 * - logic for drawing map
 * - handling map scrolling
 * - tiles downloader and decoder
 * @author Mike
 */
public class TileMapView extends SurfaceView implements SurfaceHolder.Callback, Constants {

    private static final String TAG = "TileMapView";
    
    private Context mContext;
    private DrawingTask mDrawingTask;
    // screen width and height in px
    private int mScreenWidth, mScreenHeight;
    // screen width and height in tiles
    private int mScreenWidthInTiles, mScreenHeightInTiles;
    // absolute coords of map top-left corner in px
    private volatile float mAbsoluteCoordX, mAbsoluteCoordY;
    // screen relative coords when scrolling map in px
    private float mRelativeCoordX, mRelativeCoordY;
    // absolute coords of visible screen (camera) top-left corner in px
    private volatile float mCameraX, mCameraY;
    // tiles indexes
    private int mTileIdX, mTileIdY;
    // delta when scrolling map in px
    private float mScrollDx, mScrollDy;
    private Paint mTextDebugPaint;
    private Paint mTextNoTilePaint;
    // LruCache storing bitmaps in RAM memory
    private Cache<Bitmap> mRAMCache;
    // LruCache storing bitmaps paths on disk storage
    private Cache<String> mDiskCache;
    
    public TileMapView(Context context, float x, float y, float cameraX, float cameraY, boolean needRestore) {
        super(context);
        getHolder().addCallback(this);
        mContext = context;
        getScreenInfo();
        initPaint();
        initCoords(x, y, cameraX, cameraY);
        initMap();
        initCache(needRestore);
    }

    private void getScreenInfo() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	display.getSize(size);
        	mScreenWidth = size.x;
        	mScreenHeight = size.y;
        } else {
        	mScreenWidth = display.getWidth();
        	mScreenHeight = display.getHeight();
        }
    }
    
    private void initPaint() {
        // define debug output format
        mTextDebugPaint = new Paint();
        mTextDebugPaint.setColor(Color.RED);
        mTextDebugPaint.setTextSize(65);
        mTextNoTilePaint = new Paint();
        mTextNoTilePaint.setColor(Color.BLACK);
        mTextNoTilePaint.setTextSize(50);
    }
    
    private void initCoords(float x, float y, float cameraX, float cameraY) {
        mAbsoluteCoordX = x;
        mAbsoluteCoordY = y;
        mRelativeCoordX = mRelativeCoordY = 0;
        mCameraX = cameraX;
        mCameraY = cameraY;
        mTileIdX = mTileIdY = 0;
        mScrollDx = mScrollDy = 0;
    }
    
    private void initMap() {
        TileMap.getInstance().initMap();
    }
    
    private void initCache(boolean needRestore) {
        // init cache with current screen size
        mScreenWidthInTiles = mScreenWidth / TILE_WIDTH + 2;
        mScreenHeightInTiles = mScreenHeight / TILE_HEIGHT + 2;
        mRAMCache = new LRUCache<Bitmap>(mScreenWidthInTiles * mScreenHeightInTiles);
        mDiskCache = new LRUCache<String>(CACHE_STORAGE_SIZE);
        // restore screen in case of orientation change
        if (needRestore) {
	    	File dir = new File(CACHE_STORAGE_PATH);
	    	if (dir.exists()) {
	            String[] children = dir.list();
	            for (int i = 0; i < children.length; i++) {
	            	int separator = children[i].indexOf("_");
	            	String tileXStr = children[i].substring(0, separator);
	            	int separator2 = children[i].indexOf(".");
	            	String tileYStr = children[i].substring(separator + 1, separator2);
                    int tileX = Integer.parseInt(tileXStr);
                    int tileY = Integer.parseInt(tileYStr);
                    Tile tile = TileMap.getInstance().getMap()[tileY][tileX];
                    String path = CACHE_STORAGE_PATH + tileX + "_" + tileY + ".png";
                    if (new File(path).exists()) {
                    	mDiskCache.set(tile, path);
	                	if (isTileVisible(tile)) {
                    		TileReaderTask tileReader = new TileReaderTask(tile, path, mRAMCache);
                    		tileReader.start();
	                	}
                    }
	            }
	    	}
        }
        // prepare cache storage
        File dir = new File(CACHE_STORAGE_PATH);
        if (!dir.exists())
            dir.mkdirs();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {   
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawingTask = new DrawingTask(getHolder(), getResources());
        mDrawingTask.setRunning(true);
        mDrawingTask.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        // finish drawing thread
        mDrawingTask.setRunning(false);
        while (retry) {
            try {
                mDrawingTask.join();
                retry = false;
            } catch (InterruptedException e) {
                // keep trying
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // return super.onTouchEvent(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mRelativeCoordX = event.getX();
                mRelativeCoordY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mScrollDx = event.getX() - mRelativeCoordX;
                mScrollDy = event.getY() - mRelativeCoordY;
                mRelativeCoordX = event.getX();
                mRelativeCoordY = event.getY();
                mAbsoluteCoordX += mScrollDx;
                mAbsoluteCoordY += mScrollDy;
                mCameraX += -mScrollDx;
                mCameraY += -mScrollDy;
                // check map limits
                if (mAbsoluteCoordX > 0 || mAbsoluteCoordX < mScreenWidth - TILES_NUMBER_X * TILE_WIDTH) {
                	mAbsoluteCoordX -= mScrollDx;
                	mCameraX -= -mScrollDx;
                }
                if (mAbsoluteCoordY > 0 || mAbsoluteCoordY < mScreenHeight - TILES_NUMBER_Y * TILE_HEIGHT) {
                	mAbsoluteCoordY -= mScrollDy;
                	mCameraY -= -mScrollDy;
                }
                //Log.d(TAG, "mAbsoluteCoordX="+mAbsoluteCoordX+" mAbsoluteCoordY="+mAbsoluteCoordY+" mScrollDx="+mScrollDx+" mScrollDy="+mScrollDy);
                break;
        }
        return true;
    }
    
    public float getAbsoluteCoordX() {
    	return mAbsoluteCoordX;
    }
    
    public float getAbsoluteCoordY() {
    	return mAbsoluteCoordY;
    }

    public float getCameraX() {
    	return mCameraX;
    }
    
    public float getCameraY() {
    	return mCameraY;
    }

    public Cache<String> getStorageCache() {
    	return mDiskCache;
    }
    
    private boolean isTileVisible(Tile tile) {
    	boolean isVisible = false;
        int tileX = tile.getIndexX() * TILE_WIDTH;
        int tileY = tile.getIndexY() * TILE_HEIGHT;
        if (tileX > mCameraX - TILE_WIDTH && tileX < mCameraX + mScreenWidth &&
        	tileY > mCameraY - TILE_HEIGHT && tileY < mCameraY + mScreenHeight)
        	isVisible = true;
    	return isVisible;
    }
    
    // draws on surface view
    class DrawingTask extends Thread {
        
        private volatile boolean mRunFlag = false;
        private SurfaceHolder surfaceHolder;
    	private Bitmap bufferedBitmap;
    	private Canvas bufferedCanvas;
        private float coordX, coordY;
        private int tileX, tileY;
    
        public DrawingTask(SurfaceHolder holder, Resources resources) {
            surfaceHolder = holder;
            bufferedBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
            bufferedCanvas = new Canvas(bufferedBitmap);
        }
    
        public void setRunning(boolean run) {
        	mRunFlag = run;
        }

        private void calculateCoords(float x, float y, int i, int j, int offsetX, int offsetY) {
            mTileIdX = (int) x / TILE_WIDTH;
            mTileIdY = (int) y / TILE_HEIGHT;
            tileX = Math.abs(mTileIdX) + i;
            if (tileX >= TILES_NUMBER_X)
                tileX = TILES_NUMBER_X - 1;
            tileY = Math.abs(mTileIdY) + j;
            if (tileY >= TILES_NUMBER_Y)
                tileY = TILES_NUMBER_Y - 1;
            coordX = x + (i - mTileIdX) * TILE_WIDTH - offsetX;
            coordY = y + (j - mTileIdY) * TILE_HEIGHT - offsetY;
            if (i == 0 && coordX > 0) {
                offsetX = TILE_WIDTH;
                coordX -= offsetX;
            }
            if (j == 0 && coordY > 0) {
                offsetY = TILE_HEIGHT;
                coordY -= offsetY;
            }
        }

        private void drawMap(Canvas canvas) {
            // clear the buffered bitmap
        	bufferedBitmap.eraseColor(android.graphics.Color.TRANSPARENT);
        	bufferedCanvas.drawColor(Color.GRAY);
        	
            float x = mAbsoluteCoordX;
            float y = mAbsoluteCoordY;
            int offsetY = 0;
            for (int j = 0; j < mScreenHeightInTiles; j++) {
                int offsetX = 0;
                for (int i = 0; i < mScreenWidthInTiles; i++) {
                    calculateCoords(x, y, i, j, offsetX, offsetY); //
                    Tile tile = TileMap.getInstance().getMap()[tileY][tileX]; // get current tile
                    if (isTileVisible(tile)) {
	                    Bitmap tileBitmap = mRAMCache.get(tile); // get tile bitmap from RAM cache
	                    if (tileBitmap == null) { // no bitmap in RAM cache
	                    	String path = mDiskCache.get(tile); // get tile bitmap from ROM cache
	                    	if (path != null)
                                new TileReaderTask(tile, path, mRAMCache).start();
	                    	else if (!tile.isDownloading()) // no bitmap in ROM cache, need to download
	                    		new TileDownloaderTask(mContext, tile, mRAMCache, mDiskCache).start();
		                    bufferedCanvas.drawText("No tile", coordX + TILE_WIDTH / 4,
                                    coordY + 3 * TILE_HEIGHT / 5, mTextNoTilePaint);
	                    } else {
	                    	bufferedCanvas.drawBitmap(tileBitmap, coordX, coordY, null);
	                    	//mBufferedCanvas.drawText(tile.getNumber() + "", coordX + TILE_WIDTH / 4, coordY + 2 * TILE_HEIGHT / 3, mTextDebugPaint);
		                    //Log.d(TAG, "j="+j+" i="+i+" coordX="+coordX+" coordY="+coordY+" x="+x+" y="+y+" mTileX="+mTileIdX+" mTileY="+mTileIdY);// + " num="+mTiles[Math.abs(mTileIdY) + j][Math.abs(mTileIdX) + i].getNumber() + " yTileEnd="+yTileEnd);
	                    }
                    }
                }
            }
            canvas.drawBitmap(bufferedBitmap, 0, 0, null);
        }
        
        @Override
        public void run() {
            Canvas canvas;
            while (mRunFlag) {
                canvas = null;
                try {
                    // get canvas and draw on it
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            drawMap(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        // drawing completed, display canvas on the screen
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
    
}

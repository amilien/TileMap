
package com.amilabs.android.tilemap;

import java.io.File;

import com.amilabs.android.tilemap.utils.Constants;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class TileMapActivity extends Activity implements Constants {

	private TileMapView mTileMapView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        float coordX = 0, coordY = 0, cameraX = 0, cameraY = 0;
        boolean needRestore = false;
        if (savedInstanceState != null) {
        	coordX = savedInstanceState.getFloat("absCoordX");
        	coordY = savedInstanceState.getFloat("absCoordY");
        	cameraX = savedInstanceState.getFloat("cameraX");
        	cameraY = savedInstanceState.getFloat("cameraY");
        	needRestore = true;
        }
        mTileMapView = new TileMapView(this, coordX, coordY, cameraX, cameraY, needRestore);
        setContentView(mTileMapView);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("absCoordX", mTileMapView.getAbsoluteCoordX());
        outState.putFloat("absCoordY", mTileMapView.getAbsoluteCoordY());
        outState.putFloat("cameraX", mTileMapView.getCameraX());
        outState.putFloat("cameraY", mTileMapView.getCameraY());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.action_settings:
    			new StorageCacheCleaner().start();
    			return true;
    		default:
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    // thread decodes storage cached bitmap
    class StorageCacheCleaner extends Thread {
    	
    	@Override
    	public void run() {
	    	File dir = new File(CACHE_STORAGE_PATH);
	    	if (dir.exists()) {
	            String[] children = dir.list();
	            for (int i = 0; i < children.length; i++) {
	                new File(dir, children[i]).delete();
	                mTileMapView.getStorageCache().remove(dir + "/" + children[i]);
	            }
	        }
    	}
    }

}

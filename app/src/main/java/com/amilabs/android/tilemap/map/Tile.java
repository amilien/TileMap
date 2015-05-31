package com.amilabs.android.tilemap.map;

public class Tile {

    private String mUrl;
    private int mNumber;
    private volatile boolean mIsDownloading;
	private int mIndexX;
	private int mIndexY;
	
    public Tile(String url, int number, int indexX, int indexY) {
        mUrl = url;
        mNumber = number;
        mIsDownloading = false;
        mIndexX = indexX;
        mIndexY = indexY;
    }
    
    // get tile's sequential number for debug purpose
    public int getNumber() {
        return mNumber;
    }
    
    // get tile's url
    public String getUrl() {
    	return mUrl;
    }
    
    // get tile's downloading status
    public boolean isDownloading() {
    	return mIsDownloading;
    }
    
    // set tile's downloading status
    public void setDownloading(boolean val) {
    	mIsDownloading = val;
    }
    
    public int getIndexX() {
    	return mIndexX;
    }
    
    public int getIndexY() {
    	return mIndexY;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
        int result = 1;
        result = prime * result + mIndexX;
        result = prime * result + mIndexY;
        return result;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Tile other = (Tile) obj;
        if (mIndexX != other.mIndexX || mIndexY != other.mIndexY)
            return false;
        return true;
    }

}

package com.amilabs.android.tilemap.cache;

import com.amilabs.android.tilemap.map.Tile;

/**
 * Cache interface.
 * Can be used for another cache implementation.
 * @author Mike
 *
 */
public interface Cache<T> {

	/**
	 * gets the tile bitmap
	 * @param key
	 * @return
	 */
	public T get(Tile key);

	/**
	 * sets the tile bitmap (or path to the storage cached bitmap) for the given Tile key
	 * @param key
	 * @param value
	 */
	public void set(Tile key, T value);
	
	/**
	 * removes the tile bitmap (or path to the storage cached bitmap) for the given Tile key
	 * @param key
	 * @param value
	 */
	public void remove(T value);
}

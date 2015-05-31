package com.amilabs.android.tilemap.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.amilabs.android.tilemap.map.Tile;

/**
 * Concurrent implementation of LruCache.
 * Max capacity is:
 * - for memory cache: capacity = screenWidthInTiles * screenHeightInTiles,
 * where screenWidthInTiles or screenHeightInTiles also contain 2 additional tiles for both screen sides.
 * - for storage cache: capacity = Constants.CACHE_STORAGE_SIZE
 * @author Mike
 *
 */
public class LRUCache<T> implements Cache<T> {
	
	private Map<Tile, DoubleLinkedListNode> mMap;
	private DoubleLinkedListNode mHead;
	private DoubleLinkedListNode mEnd;
	private int mCapacity;
	private int mLength;
	private final Lock mLock;
 
	public LRUCache(int capacity) {
		mCapacity = capacity;
		mLength = 0;
		mMap = new HashMap<Tile, DoubleLinkedListNode>();
		mLock = new ReentrantLock(true);
	}
 
	public Map<Tile, DoubleLinkedListNode> getMap() {
		return mMap;
	}
	
	public T get(Tile key) {
		mLock.lock();
		try {
			if (mMap.containsKey(key)) {
				DoubleLinkedListNode latest = mMap.get(key);
				removeNode(latest);
				setHead(latest);
				return latest.mValue;
			} else
				return null;
		} finally {
			mLock.unlock();
		}
	}
 
	private void removeNode(DoubleLinkedListNode node) {
		DoubleLinkedListNode cur = node;
		DoubleLinkedListNode pre = cur.mPrev;
		DoubleLinkedListNode post = cur.mNext;
		if (pre != null) {
			pre.mNext = post;
		} else {
			mHead = post;
		}
		if (post != null) {
			post.mPrev = pre;
		} else {
			mEnd = pre;
		}
	}
 
	private void setHead(DoubleLinkedListNode node) {
		node.mNext = mHead;
		node.mPrev = null;
		if (mHead != null) {
			mHead.mPrev = node;
		}
		mHead = node;
		if (mEnd == null) {
			mEnd = node;
		}
	}
 
	public void set(Tile key, T value) {
		mLock.lock();
		try {
			if (mMap.containsKey(key)) {
				DoubleLinkedListNode oldNode = mMap.get(key);
				oldNode.mValue = value;
				removeNode(oldNode);
				setHead(oldNode);
			} else {
				DoubleLinkedListNode newNode = new DoubleLinkedListNode(key, value);
				if (mLength < mCapacity) {
					setHead(newNode);
					mMap.put(key, newNode);
					mLength++;
				} else {
					mMap.remove(mEnd.mKey);
					mEnd = mEnd.mPrev;
					if (mEnd != null) {
						mEnd.mNext = null;
					}
					setHead(newNode);
					mMap.put(key, newNode);
				}
			}
		} finally {
			mLock.unlock();
		}
	}
	
	public void remove(T value) {
		mLock.lock();
		try {
			for (Iterator<Map.Entry<Tile, DoubleLinkedListNode>> it = mMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Tile, DoubleLinkedListNode> entry = it.next();
				Tile key = entry.getKey();
				DoubleLinkedListNode node = mMap.get(key);
				if (value.equals(node.mValue)) {
					removeNode(node);
					it.remove();
					mLength--;
				}
			}
		} finally {
			mLock.unlock();
		}
	}
	
	class DoubleLinkedListNode {
		
		public Tile mKey;
		public T mValue;
		public DoubleLinkedListNode mPrev;
		public DoubleLinkedListNode mNext;
	 
		public DoubleLinkedListNode(Tile key, T value) {
			mKey = key;
			mValue = value;
		}
	}
	
}

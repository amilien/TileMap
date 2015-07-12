# TileMap
This is an example of a customized MapView.
Map is based on tiles which are stored as images on the remote server.

Loading tiles when scrolling map:

![Alt text](https://github.com/amilien/TileMap/blob/master/scrsht/map1.png "")

All visible tiles are loaded:

![Alt text](https://github.com/amilien/TileMap/blob/master/scrsht/map2.png "")

Current MapView implementation supports:
- scrolling
- memory cache (RAM-based)
- storage cache (ROM-based)

As a cache data structure LRUCache is used, which has been implemented from the scratch.

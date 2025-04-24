package ru.roms2002.messenger.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class RoomCacheService {

	@Autowired
	private CacheManager cacheManager;

	private Cache getCache() {
		return cacheManager.getCache("rooms");
	}

	public void putNewRoom(String groupUrl, String roomUrl, ArrayList<Integer> usersList) {
		StringBuilder key = new StringBuilder();
		key.append(groupUrl);
		key.append("_");
		key.append(roomUrl);
		Cache roomsCache = this.getCache();
		roomsCache.put(key.toString(), usersList);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllKeys() {
		ConcurrentHashMap<String, HashMap<String, ArrayList<Integer>>> map = (ConcurrentHashMap<String, HashMap<String, ArrayList<Integer>>>) this
				.getCache().getNativeCache();
		return Collections.list(map.keys());
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, ArrayList<Integer>> getRoomByKey(String groupUrl) {
		HashMap<String, ArrayList<Integer>> hostsList = new HashMap<>();
		Cache roomsCache = this.getCache();
		if (roomsCache != null) {
			Cache.ValueWrapper valueWrapper = roomsCache.get(groupUrl);
			if (valueWrapper != null) {
				hostsList = (HashMap<String, ArrayList<Integer>>) valueWrapper.get();
			}
		}
		return hostsList;
	}
}

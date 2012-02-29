package org.NooLab.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * * Reverse a Map, such that the keys become
 * * the values and vice versa. The former
 * * need to be held in a List owing to the
 * * possible presence of duplicates
 * *
 * * @author Charles Johnson
 * * @version 1.0
 * *
 * * @param <K> The key type of the original map
 * * @param <V> The value type of the original map
 * */
public class MapReverser<K, V> {
        private Map<K, V> map;

        public MapReverser(Map<K, V> map) {
                this.map = map;
        }

        public Map<V, List<K>> getMap() {
                Map<V, List<K>> reversed = new HashMap<V, List<K>>();
                Iterator<Map.Entry<K, V>> i = map.entrySet().iterator();

                while (i.hasNext()) {
                        Map.Entry<K, V> e = i.next();
                        K key = e.getKey();
                        V value = e.getValue();
                        List<K> reversedKeys = null;

                        if (reversed.containsKey(value)) {
                                reversedKeys = reversed.get(value);
                        } else {
                                reversedKeys = new ArrayList<K>();
                                reversed.put(value, reversedKeys);
                        }

                        reversedKeys.add(key);
                }

                return reversed;
        }
}

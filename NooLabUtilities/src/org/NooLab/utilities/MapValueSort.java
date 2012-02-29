package org.NooLab.utilities;

import java.util.*;


public class MapValueSort {
    public static void main(String[] args) {
        Map<String, String> m = new HashMap<String, String>();
        m.put("zero", "0");
        m.put("nil", "0");
        m.put("one", "1");

        List<Map.Entry<String, String>> entries = MapValueSort.sortByValue(m);
        System.out.println(entries);
    }

    @SuppressWarnings("unchecked")
    public static <K, V extends Comparable> List<Map.Entry<K, V>> sortByValue(
        Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(map.size());
        entries.addAll(map.entrySet());
        Collections.sort(entries,
            new Comparator<Map.Entry<K, V>>() {
                public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                    return e1.getValue().compareTo(e2.getValue());
                }
            });

        return entries;
    }
}

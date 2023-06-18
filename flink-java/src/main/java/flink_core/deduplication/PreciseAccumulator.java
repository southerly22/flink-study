package flink_core.deduplication;

import org.roaringbitmap.longlong.Roaring64NavigableMap;

/**
 * @author lzx
 * @date 2023/6/18 18:23
 * @description: TODO
 */
public class PreciseAccumulator {
    private Roaring64NavigableMap bitmap;

    public PreciseAccumulator() {
        bitmap = new Roaring64NavigableMap();
    }
    public void add(long id){
        bitmap.addLong(id);
    }
    public long getCardinality(){
        return bitmap.getLongCardinality();
    }
}

package flink_core.deduplication;

import org.apache.flink.table.functions.AggregateFunction;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

/**
 * @author lzx
 * @date 2023/6/18 18:16
 * @description: TODO
 */
public class PreciseDistinct extends AggregateFunction<Long, PreciseAccumulator> {

    @Override
    public PreciseAccumulator createAccumulator() {
        return new PreciseAccumulator();
    }

    public void accumulate(PreciseAccumulator preciseAccumulator,Long id){
        preciseAccumulator.add(id);
    }

    @Override
    public Long getValue(PreciseAccumulator preciseAccumulator) {
        return preciseAccumulator.getCardinality();
    }
}

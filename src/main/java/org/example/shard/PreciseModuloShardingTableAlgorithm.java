package org.example.shard;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public final class PreciseModuloShardingTableAlgorithm implements PreciseShardingAlgorithm<Integer> {

    @Override
    public String doSharding(final Collection<String> tableNames, final PreciseShardingValue<Integer> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(Integer.toString(shardingValue.getValue() % 2))) {
                System.out.println("each = " + each);
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
}

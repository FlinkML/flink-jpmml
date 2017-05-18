package io.radicalbit.flink.pmml.java.strategies;

import org.dmg.pmml.DataType;

import java.util.Map;

public class MissingValueStrategies {
    public static MissingValueStrategy delegateToPMMLStrategy() {
        return new DelegateToPMMLStrategy();
    }

    public static MissingValueStrategy defaultValueStrategy(Map<DataType, Object> m) {
        return new DefaultValueStrategy(m);
    }

    public static MissingValueStrategy propagateExceptionStrategy(){
        return new PropagateExceptionStrategy();
    }
}
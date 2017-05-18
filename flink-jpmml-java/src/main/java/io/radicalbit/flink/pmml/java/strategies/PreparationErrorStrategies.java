package io.radicalbit.flink.pmml.java.strategies;

public class PreparationErrorStrategies {
    public static PreparationErrorStrategy propagateExceptionStrategy() {
        return new PropagateExceptionStrategy();
    }
}
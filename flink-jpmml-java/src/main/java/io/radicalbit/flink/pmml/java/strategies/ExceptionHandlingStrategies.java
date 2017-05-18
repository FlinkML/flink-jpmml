package io.radicalbit.flink.pmml.java.strategies;

public class ExceptionHandlingStrategies {
    public static ExceptionHandlingStrategy logExceptionStrategy() {
        return new LogExceptionStrategy();
    }
}
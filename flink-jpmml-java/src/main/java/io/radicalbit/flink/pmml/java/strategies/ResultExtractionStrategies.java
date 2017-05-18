package io.radicalbit.flink.pmml.java.strategies;

public class ResultExtractionStrategies {
    public static ResultExtractionStrategy extractTargetAndOutputFieldStrategy() {
        return new ExtractTargetAndOutputFieldStrategy();
    }

    public static ResultExtractionStrategy getRawResultStrategy() {
        return new GetRawResultStrategy();
    }
}
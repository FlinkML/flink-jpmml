package io.radicalbit.flink.pmml.java.strategies;

import java.io.Serializable;

/**
 * Strategy to capture and process exceptions raised in an Operator.
 */
public interface ExceptionHandlingStrategy extends Serializable {
    void handleException(Exception e);
}

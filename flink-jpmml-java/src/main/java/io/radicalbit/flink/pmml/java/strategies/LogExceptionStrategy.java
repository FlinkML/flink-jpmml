package io.radicalbit.flink.pmml.java.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Log an exception at ERROR level.
 */
public final class LogExceptionStrategy implements ExceptionHandlingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LogExceptionStrategy.class);

    protected LogExceptionStrategy() {
    }

    /**
     * This function log an exception and its cause using the SLF4J dedicated API.
     * @param e
     */
    @Override
    final public void handleException(Exception e) {

        logger.error(e.getMessage(),e);
    }
}

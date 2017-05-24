/*
 *
 * Copyright (c) 2017 Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 *         
 */

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

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

import io.radicalbit.flink.pmml.java.InputPreparationException;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.ModelEvaluator;

import java.util.Map;


/**
 * This strategy replace every missing value with a default value depending on the DataType.
 * If a default value is not specified for a DataType and a missing value of that DataType is found,
 * an exception is raised.
 */
public class DefaultValueStrategy implements MissingValueStrategy {

    private final Map<DataType, Object> defaultMap;

    protected DefaultValueStrategy(Map<DataType, Object> defaultMap_t) {
        defaultMap = defaultMap_t;
    }

    @Override
    public final Object handleError(FieldName fieldName, ModelEvaluator evaluator) {
        final DataType type = evaluator.getDataField(fieldName).getDataType();
        if (defaultMap.containsKey(type)) {
            return defaultMap.get(type);
        } else {
            throw new InputPreparationException("Default value for type " + type.value() + " was not found.");
        }

    }
}

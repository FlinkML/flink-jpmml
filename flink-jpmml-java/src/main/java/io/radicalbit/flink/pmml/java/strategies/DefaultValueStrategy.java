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

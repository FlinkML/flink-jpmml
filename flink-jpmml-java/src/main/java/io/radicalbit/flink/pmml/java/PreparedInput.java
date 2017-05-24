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

package io.radicalbit.flink.pmml.java;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;

import java.util.Map;

/**
 * Data class wrapping the result of an input preparation.
 */
public class PreparedInput {
    private final Map<FieldName, FieldValue> preparedData;
    private final Evaluator evaluator;

    public PreparedInput(Map<FieldName, FieldValue> data, Evaluator evaluator_t) {
        preparedData = data;
        evaluator = evaluator_t;

    }

    public final EvaluationResult evaluate() {
        final Map<FieldName, Object> result = (Map<FieldName, Object>) evaluator.evaluate(this.preparedData);
        return new EvaluationResult(result, evaluator);
    }

    public Map<FieldName, FieldValue> getPreparedData() {
        return preparedData;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

}

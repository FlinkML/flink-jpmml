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
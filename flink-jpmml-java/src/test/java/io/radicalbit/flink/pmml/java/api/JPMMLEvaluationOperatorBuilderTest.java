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

package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.PMMLTestUtils;
import io.radicalbit.flink.pmml.java.strategies.*;
import org.dmg.pmml.DataType;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class JPMMLEvaluationOperatorBuilderTest {


    /**
     * To be changed according to eventual new default strategies. This test works as a specification for the default strategies.
     * In case the default strategies are willingly changed, adapt this test accordingly.
     */
    @Test
    public void testDefault() throws IOException, SAXException, JAXBException {
        String pmml = PMMLTestUtils.irisPMMLSource();
        JPMMLEvaluationOperatorBuilder builder = JPMMLEvaluationOperatorBuilder.create(pmml);

        JPMMLEvaluationFlatMapOperator fmo = builder.buildFlatMapOperator();
        JPMMLEvaluationMapOperator mo = builder.buildMapOperator();

        assertTrue(fmo.getExceptionHandlingStrategy() instanceof LogExceptionStrategy);
        assertTrue(fmo.getPreparationErrorStrategy() instanceof PropagateExceptionStrategy);
        assertTrue(fmo.getResultExtractionStrategy() instanceof ExtractTargetAndOutputFieldStrategy);
        assertTrue(fmo.getMissingValueStrategy() instanceof PropagateExceptionStrategy);

        assertTrue(mo.getPreparationErrorStrategy() instanceof PropagateExceptionStrategy);
        assertTrue(mo.getResultExtractionStrategy() instanceof ExtractTargetAndOutputFieldStrategy);
        assertTrue(mo.getMissingValueStrategy() instanceof PropagateExceptionStrategy);

    }

    @Test
    public void testSet() throws IOException, SAXException, JAXBException {
        String pmml = PMMLTestUtils.irisPMMLSource();
        JPMMLEvaluationOperatorBuilder builder = JPMMLEvaluationOperatorBuilder.create(pmml);

        builder.setMissingValueStrategy(MissingValueStrategies.defaultValueStrategy(new HashMap<DataType, Object>()));

        JPMMLEvaluationFlatMapOperator fmo = builder.buildFlatMapOperator();
        JPMMLEvaluationMapOperator mo = builder.buildMapOperator();

        assertTrue(fmo.getMissingValueStrategy() instanceof DefaultValueStrategy);
        assertTrue(mo.getMissingValueStrategy() instanceof DefaultValueStrategy);
    }
}

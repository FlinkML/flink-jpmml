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

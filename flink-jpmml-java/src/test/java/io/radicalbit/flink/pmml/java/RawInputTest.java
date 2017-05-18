package io.radicalbit.flink.pmml.java;

import io.radicalbit.flink.pmml.java.strategies.MissingValueStrategies;
import io.radicalbit.flink.pmml.java.strategies.PreparationErrorStrategies;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.FieldValueUtil;
import org.jpmml.evaluator.ModelEvaluator;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RawInputTest {


    Map<String,Object> sample;
    ModelEvaluator<? extends Model> evaluator;
    @Before
    public void initSample() throws IOException, SAXException, JAXBException {
        sample= PMMLTestUtils.simpleIrisInput();
        evaluator = PMMLTestUtils.irisEvaluator();
    }
    @Test
    public void prepareSuccess() throws Exception {
        RawInput r = new RawInput(sample);

        PreparedInput pi=r.prepare(evaluator, PreparationErrorStrategies.propagateExceptionStrategy(), MissingValueStrategies.delegateToPMMLStrategy());

        Map<FieldName,FieldValue> expectedResult =new HashMap<>();
        expectedResult.put(new FieldName("sepal_width"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("sepal_length"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_width"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_length"), FieldValueUtil.create(null, null, 1.0));

        assertEquals(pi.getPreparedData(), expectedResult);

    }
    @Test
    public void prepareMissingDelegate() throws Exception {
        sample.remove("sepal_width");
        RawInput r = new RawInput(sample);

        PreparedInput pi=r.prepare(evaluator, PreparationErrorStrategies.propagateExceptionStrategy(), MissingValueStrategies.delegateToPMMLStrategy());

        Map<FieldName,FieldValue> expectedResult =new HashMap<>();
        expectedResult.put(new FieldName("sepal_width"), null);
        expectedResult.put(new FieldName("sepal_length"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_width"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_length"), FieldValueUtil.create(null, null, 1.0));

        assertEquals(pi.getPreparedData(), expectedResult);

    }

    @Test(expected = InputPreparationException.class)
    public void prepareMissingException() throws Exception {
        sample.remove("sepal_width");
        RawInput r = new RawInput(sample);

        r.prepare(evaluator, PreparationErrorStrategies.propagateExceptionStrategy(),
                MissingValueStrategies.propagateExceptionStrategy());
    }

    @Test
    public void prepareMissingDefault() throws Exception {
        Map<DataType,Object> defaults = new HashMap<>();
        defaults.put(DataType.DOUBLE,5.0);

        sample.remove("sepal_width");
        RawInput r = new RawInput(sample);

        Map<FieldName,FieldValue> expectedResult =new HashMap<>();
        expectedResult.put(new FieldName("sepal_width"), FieldValueUtil.create(null, null, 5.0));
        expectedResult.put(new FieldName("sepal_length"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_width"), FieldValueUtil.create(null, null, 1.0));
        expectedResult.put(new FieldName("petal_length"), FieldValueUtil.create(null, null, 1.0));

        PreparedInput pi=r.prepare(evaluator, PreparationErrorStrategies.propagateExceptionStrategy(),
                MissingValueStrategies.defaultValueStrategy(defaults));


        assertEquals(pi.getPreparedData(),expectedResult);
    }

    @Test(expected = InputPreparationException.class)
    public void prepareMissingDefaultFailing() throws Exception {
        Map<DataType,Object> defaults = new HashMap<>();
        defaults.put(DataType.INTEGER,1);

        sample.remove("sepal_width");
        RawInput r = new RawInput(sample);

        r.prepare(evaluator, PreparationErrorStrategies.propagateExceptionStrategy(),
                MissingValueStrategies.defaultValueStrategy(defaults));

    }

}

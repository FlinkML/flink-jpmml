package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.EvaluationResult;
import io.radicalbit.flink.pmml.java.PreparedInput;
import io.radicalbit.flink.pmml.java.RawInput;
import io.radicalbit.flink.pmml.java.strategies.MissingValueStrategy;
import io.radicalbit.flink.pmml.java.strategies.PreparationErrorStrategy;
import io.radicalbit.flink.pmml.java.strategies.ResultExtractionStrategy;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.Map;

public class JPMMLEvaluationOperatorUtil {

    /**
     * Default processing logic. Used by JPMMLEvaluationFlatMapOperator and
     * JPMMLEvaluationMapOperator.
     *
     * @param input
     * @param preparationErrorStrategy
     * @param missingValueStrategy
     * @param evaluator
     * @param resultExtractionStrategy
     * @return
     * @throws Exception
     */

    public static Map<String, Object> process(Map<String, Object> input,
                                              PreparationErrorStrategy preparationErrorStrategy,
                                              MissingValueStrategy missingValueStrategy,
                                              ModelEvaluator<? extends Model> evaluator,
                                              ResultExtractionStrategy resultExtractionStrategy
    ) throws Exception {

        final RawInput i = new RawInput(input);
        final PreparedInput pi = i.prepare(evaluator, preparationErrorStrategy, missingValueStrategy);
        final EvaluationResult result = pi.evaluate();
        return resultExtractionStrategy.extract(result);
    }

    public static ModelEvaluator<? extends Model> unmarshallPMML(String pmmlSource) throws JAXBException, SAXException {
        final ModelEvaluatorFactory factory = ModelEvaluatorFactory.newInstance();
        return factory.newModelEvaluator(JAXBUtil.unmarshalPMML(fromFilteredSource(pmmlSource)));
    }

    private static SAXSource fromFilteredSource(String sourcePath) throws SAXException {
        return JAXBUtil.createFilteredSource(new InputSource(new StringReader(sourcePath)), new ImportFilter());
    }
}

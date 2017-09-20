## Running simple examples
This module contains the examples running simple predictions from an iris Source.
The source emits the following data: 
```
Iris(,sepalLength: Double, sepalWidth: Double, petalLength: Double, petalWidth: Double)
```
In order to keep the example run

1) Run `sbt` command in project dir and select project:
```
project flink-jpmml-handson
```

2) Create a `.jar`:
```
assembly
``` 

3) Run the examples. If you want full predictions:
```
./path/to/bin/flink run -c io.radicalbit.examples.EvaluateKmeans <path/to/project/root>/flink-jpmml/flink-jpmml-examples/target/scala-2.x/flink-jpmml-examples-assembly-0.6.0-SNAPSHOT.jar --model path/to/pmml/model.pmml --output /path/to/output
```
Either you can employ the _quick_ predictor:
```
./path/to/bin/flink run -c io.radicalbit.examples.QuickEvaluateKmeans <path/to/project/root>/flink-jpmml/flink-jpmml-examples/target/scala-2.x/flink-jpmml-examples-assembly-0.6.0-SNAPSHOT.jar --model path/to/pmml/model.pmml --output /path/to/output
```

## Fault-Tolerant

if you like testing a fault-tolerant of the operator you can run a `CheckpointEvaluate` example.

In order to do that: 

1) Create a socket in your local machine:
```
nc -l -k 9999
```

2) Run the flink-cluster



Both above jobs log out predictions to output path.
## Running simple examples
This module contains the examples running simple predictions from an iris Source.
The source emits the following data: 
```
Iris(sepalLength: Double, sepalWidth: Double, petalLength: Double, petalWidth: Double)
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

## Fault-Tolerance

_if you like testing the fault-tolerance behaviour of the operator you can run a `CheckpointEvaluate` example._

In order to do that: 

1) Create a socket in your local machine:
```
nc -l -k 9999
```

2) Run the flink-cluster( [Flink 1.3.2](http://flink.apache.org/downloads.html#binaries) is required ):
```
./path/to/flink-1.3.2/start-cluster.sh
```

3) run the flink job:
```
./path/to/bin/flink run -c io.radicalbit.examples.CheckpointEvaluate <path/to/project/root>/flink-jpmml/flink-jpmml-examples/target/scala-2.x/flink-jpmml-examples-assembly-0.6.0-SNAPSHOT.jar --output /path/to/output
```

4) Send the model via socket, in this case you can use the models in `flink-jpmml-assets`:
```
<path/to/project/root>/flink-jpmml/flink-jpmml-assets/resources/kmeans.xml
<path/to/project/root>/flink-jpmml/flink-jpmml-assets/resources/kmeans_nooutput.xml

```

6) Stop the task manager: 
```
./path/to/flink-1.3.2/bin/taskmanager.sh stop
```
_you can see the job's status in Flink UI on http://localhost:8081_


7) Restart the task manager:
```
./path/to/flink-1.3.2/bin/taskmanager.sh start
```

_At this point, when the job is restarted, there's no need to re-send the models info by control stream because you should see the models from the last checkpoint_ 



Note: Both above jobs log out predictions to output path.
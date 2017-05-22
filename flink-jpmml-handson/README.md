## Running Examples
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
./path/to/bin/flink run -c io.radicalbit.examples.Clustering --model path/to/pmml/model.pmml --output /path/to/output
```
Either you can employ the _quick_ predictor:
```
./path/to/bin/flink run -c io.radicalbit.examples.QuickClustering --model path/to/pmml/model.pmml --output /path/to/output
```


Both above jobs log out predictions to Flink `log/` directory.
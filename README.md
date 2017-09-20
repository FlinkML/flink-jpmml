[![Build Status](https://travis-ci.org/FlinkML/flink-jpmml.png)](https://travis-ci.org/FlinkML/flink-jpmml)

# flink-jpmml 

Welcome! `flink-jpmml` is a fresh-made library for **real time** machine learning predictions built on top of 
[PMML](http://dmg.org/pmml/v4-3/GeneralStructure.html) standard models and [Apache Flink](https://flink.apache.org/) 
streaming engine.

`flink-jpmml`is ease to use, running at **serious** scale, backend **independent** and naturally shaped to streaming 
scenario.

## Prerequisites 

In order to getting started, you only need
* any well-known version of a PMML model (**3.2** or above)
* flink-jpmml is tested with the latest Flink (i.e. 1.3.2), but any working Apache Flink version 
([repo](https://github.com/apache/flink)) should work properly.

## Adding `flink-jpmml` dependency
* if you employ sbt add the following dependecy to your project:
    * Snapshot: `"io.radicalbit" %% "flink-jpmml-scala" % "0.7.0-SNAPSHOT"`
    * Stable: `"io.radicalbit" %% "flink-jpmml-scala" % "0.6.0"`

* For [maven](https://maven.apache.org/) users instead:
    * Snapshot
    ```
    <dependencies>
        <dependency>
            <groupId>io.radicalbit</groupId>
            <artifactId>flink-jpmml-scala</artifactId>
            <version>0.7.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```
    * Stable:
    ```
    <dependencies>
        <dependency>
            <groupId>io.radicalbit</groupId>
            <artifactId>flink-jpmml-scala</artifactId>
            <version>0.6.0</version>
        </dependency>
    </dependencies>
    ```


Eventually, you can publish `flink-jpmml` on your local repository. Then 

1. execute within the flink-jpmml root
```
> sbt
```
2. select flink-jpmml-scala project
```
> project flink-jpmml-scala
```
3. publish the library to your local repo
```
> publishLocal
```

Keep in mind you will need also Flink `scala-core` `flink-streaming` and `flink-clients` libraries.

Lets start.

## Getting Started 

`flink-jpmml` enables Flink users to execute real time predictions based on machine learning models trained by any 
system supporting the PMML
standard; this allows efficient streaming model serving along with the **powerful** Flink engine features.

Lets start with a concrete example.

### Dynamic Models Evaluation

Since `0.6.0` this project supports dynamic streaming model serving efficiently. For more information 
we suggest to watch the [related talk](https://www.youtube.com/watch?v=0rWvMZ6JSD8&t=7s) 
presented at last Flink Forward 2017 in Berlin.

First of all, we indentify univocally models by the `ModelId` abstraction, made of an *ApplicationName*
and a *version*. e.g. Suppose you have two Ensemble models A and B (PMML based) where A has a depth level 10 on width 
100 and B depth 5 on width 200, and you desire to have a comparison between them, so likely your can identify 
`ApplicationName` SVM and `versions` A and B.

`flink-jpmml` do not store models within its operator state, but related metadata information.
The operator is able to retrieve models from your distributed backend exploiting the concept of
metadata table. Then, your PMML models has to be persisted in a backend system 
(see [here](#dist-backend) for supported systems).

If you want to use dynamic model evaluation you're going to define the following streams:

- `DataStream[ServingMessage]` this stream is the main user tool to feed the operator with
necessary model information; the user here is not demanded to send by stream its PMML models but only the requested 
descriptive metadata. The user should employ `ServingMessage` ADT in order to feed this stream. By now, the user can define the following two 
messages:
    - `AddMessage` it requires an `applicationName` Java UUID formatted, a `version`, the model source
    `path` and a timestamp
    - `DelMessage` it requires an `applicationName` Java UUID formatted, a `version` and a timestamp
- `DataStream[BaseEvent]` your input stream should extend the `BaseEvent` trait and defining the modelId
as a String formatted as `"<modelApplication>_<modelVersion>"` and a timestamp.

### The syntax

Given the streams above you can achieve predictions way easily.

```
import io.radicalbit.flink.pmml.scala._
import org.apache.flink.ml.math.Vector
import org.apache.flink.streaming.api.scala._
import io.radicalbit.flink.pmml.scala.models.control.ServingMessage

val inputStream: DataStream[_ <: BaseEvent] = yourInputStream
val controlStream: DataStream[ServingMessage] = yourControlStream

val predictions = 
    inputStream
        .withSupportStream(controlStream)
        .evaluate { (event, model) =>
            val vector = event.toVector
            val prediction = model.predict(vector)
            
            prediction
        }
```

The features of flink-jpmml PMML models are better discussed [here](#single-model): you will find several ways to
handle your predictions. We kept alsocthe single operator model explained later if you want to bind a specific model to
an operator instance.

### What happens internally

![flink-jpmml-architecture]
(flink-jpmml/flink-jpmml-assets/src/main/resources/architecture.png)

When an event **A** comes, it declares by its `modelId` which is the model it needs to be evaluated against.
If the model has not been uploaded within the operator yet, the latter will exploit the **metadata** information
to lazily retrieve the targeted model from the underlying distributed backend.

The control stream is the right tool for the user to provide the global picture of the models available to your
platform (this well fits a **model repository server** concept).

If the events are able to find the model the prediction is computed as a `Prediction` ADT, otherwise
the outcome will be an `EmptyPrediction`.

### <a name = "single-model"></a> Single Model Operator

Supposing you have your focused `InputStream` and you want to score related data
```
import org.apache.flink.streaming.api.scala._

case class InputEvent(data: Array)

val env = StreamExecutionEnvironment.getExecutionEnvironment
val events: DataStream[InputEvent] = env.yourInputStream
```

So you can achieve it easily with the following
```
// This will be all that you need
import io.radicalbit.flink.pmml.scala._

import org.apache.flink.ml.math.Vector
import org.apache.flink.streaming.api.scala._


object FlinkJpmmlExample {
  def main(args: Array[String]): Unit = {

    // your model can reside in any Flink supported backend
    val pathToPmml = "/even/to/distributed/systems"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val events = env.yourInputStream

    //  a lazy reader implementation
    val reader = ModelReader(pathToPmml)

    // lets go with predictions
    events.evaluate(reader) { (event, model) =>
      // FlinkML Vector abstraction is used
      val toBePredicted = vectorize(event)

      // and here we are
      val prediction: Prediction = model.predict(toBePredicted)
      val result = prediction.value

      // finally custom returns
      (event, result)
    }

    env.execute("Awesome predictions with flink-jpmml")
  }
}


```

Some useful insights from the code:
- in order to load the PMML model, you need to specify only the PMML source path
- `ModelReader` is a lazy reader and it provides the right reading abstraction to TaskManagers
- The resulting `PMMLModel` will be **loaded by once** factory for each TaskManager running on your architecture at 
_construction time_
- the `PmmlModel.predict` method expects Flink Vectors as input event and, if you want to manage NaNs, an optional 
replace value;
- `Prediction` provides the result of the input event evaluation against the PMML model as a `Prediction[Double]` ADT, 
so if the model can't manage a prediction it will return a `EmptyPrediction` value.

`flink-jpmml` provides also a quick prediction function if it can run against a Stream of Flink Vectors

```
...
val vectorStream: DataStream[Vector] = events.map(event => vectorize(event))
val predictions: DataStream[Prediction, Vector] = vectorStream.evaluate(reader)

predictions.map(_.target).print()

env.execute("flink-jpmml quick predictions")
```

These really basic examples show you how to interact with the library. The following sections try to address some 
interesting details which worth a deeper analysis.

## Behind the scenes 
`flink-jpmml` main effort is to retain all the streaming concepts:

- <a name="dist-backend"></a>since Flink is able to run against several **distributed backend**, users need to specify only the PMML source path: 
the library will take care how to load the model in full compliance of the underlying distributed system 
(e.g. HDFS, Alluxio, S3, localFS)
- `ModelReader` is the object implementing the previous behavior; it will provide the loading methods but will read it 
_lazily_, _i.e._ only when the transformation will be applied
- The `PMMLModel` will be loaded by a singleton model factory for each TaskManager running on your architecture; that means if you have an
active TaskManager _A_ made up of 4 TaskSlots, your TM will load the model from a single loader entity; this is 
crucial in order to let the system scale in thread-safety (still simple PMML models can grow to several hundreds of MegaBytes 
proportionally to the model size, meaning a big load in memory terms)
- the `PmmlModel.predict` method expects Flink Vectors as input events; this choice let us to leverage the underlying 
Breeze implementation and **no reflection** will be applied at all; moreover, the user don't have to specify any 
key-value structure: you have data matching a feature vector, so the former will be used against the latter; 
(see [input discussion](#input-abstraction) section for further details)
- `flink-jpmml` can also handle sparse data, thus you can just pass the desired _replace value_ as argument to the 
discussed method (here you will need a SparseVector) ```val prediction = model.predict(sparseData, replaceValue)```
- `Prediction` is the output case class: it returns the result of the input event evaluation against the PMML model as 
a `Score[Double]` ADT, so if the model can't manage a prediction it will return a `EmptyScore`.

 The design worths bit more focus: the choice to have a UDF as input prediction method is justified by the need of 
 handling a Machine Learning task (a prediction task) along with a pure Streaming application; in this way the user can 
 manage predictions in the body of the function with the primitive event.

## Input Vectors Abstraction 
<a name="input-abstraction"></a>

Assume this is the considered PMML feature vector
```
["sepal_width", "sepal_length", "petal_width", "petal_length"]
```
and you have values for all these fields; so, just create a DenseVector
```
val vector: DenseVector = DenseVector(value1, value2, value3, value4)
```
Suppose you missed value2, so you will need a SparseVector

```
val vector: SparseVector = SparseVector(4, Array(0, 2, 3), Array(value1, value2, value4))
```

`flink-jpmml` will recognize missing values and it will replace them with `replaceValue` if specified
(as second argument of the `PmmlModel.predict` method), otherwise the NaNs handling is demanded to the PMML model.

Note also `flink-jpmml` assumes that if you employ a DenseVector, it means that the dense instance size is your model 
size and it will not predict anything (i.e. returns `Score.Empty`); in case of sparse instances, 
the library reads the sparse size value.


## How `flink-jpmml` handles prediction issues 

`flink-jpmml` won't break your job if something goes wrong but the model loading step; it is such a crucial
action (it's mandatory for predictions), then in case of failure it raises a `ModelLoadingException`. 
Each other issue is detailed as log messages. 

The handled failures are:
- _Input Validation failure_ - The input is not corresponding to the feature vector in size terms (either too big or 
too short)
- _Input Preparation failure_ - JPMML library fails to prepare internal data format
- _Evaluation step failure_ - JPMML fails to evaluate the input against the PMML
- _JPMML Result Extraction failure_ - The job fails to retrieve the result from the PMML model

## How to Contribute 

If you want to contribute to the project send an email to [info@radicalbit.io](info@radicalbit.io) or just open an issue
here. `flink-jpmml` community is looking for you!

## Authors
* **Andrea Spina** - [andrea.spina@radicalbit.io](mailto:andrea.spina@radicalbit.io) [@spi-x-i](https://github.com/spi-x-i)
* **Francesco Frontera** - [francesco.frontera@radicalbit.io](mailto:francesco.frontera@radicalbit.io) [@francescofrontera](https://github.com/francescofrontera)
* **Simone Robutti** - *Initial prototype* [simone.robutti@gmail.com](mailto:simone.robutti@gmail.com) [@chobeat](https://github.com/chobeat)
* **Stefano Baghino** - *Initial prototype* [@stefanobaghino](https://github.com/stefanobaghino)

## Disclaimer 

* Apache®, Apache Flink™, Flink™, and the Apache feather logo are trademarks of 
[The Apache Software Foundation](http://apache.org/).
* PMML standard is a trademark of The [Data Mining Group](www.dmg.org). All rights reserved.
* JPMML-Evaluator is licensed under the GNU Affero General Public License (AGPL) version 3.0. 
Other licenses are available on request.

## License

This library has been published under the GNU Affero General Public License (AGPL) version 3.0 following and respecting [the official 
advices coming from the Apache Software Foundation](https://www.apache.org/licenses/GPL-compatibility.html) about the compatibility between 
the Apache License, Version 2.0 and the GNU General Public License, Version 3.0

-----

<SUB>THIS STANDARD IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS RELEASE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</SUB>

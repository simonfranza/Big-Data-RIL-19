# RIL 2019 - Big data introduction

## How to use it

### Setting up the docker environment
At the project's root folder, execute the following command : 
```sh
    docker-compose up -d
```

Once this command releases your terminal, enter the master container's bash using
```sh
docker-compose exec hadoop-master bash
```

Once inside, execute the following commands in the same order as described :

```sh
./start-hadoop.sh
hadoop fs -mkdir -p input
hadoop fs -put purchases.txt input
```

Make sure to execute the `start-hadoop` script everytime you restart your container.


### How to create, compile and send applications to the master container.

To create a new maven project, type the following command in your host machine : 
```sh
mvn archetype:generate -DgroupId=spark.batch -DartifactId=wordcount -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.0 -DinteractiveMode=false
```

Where :
* `DgroupId` is the company / group / team identifier
* `DartifactId` is the project's name



To compile it into a jar file, type the following command at the root directory of your maven project : 
```sh
mvn compile
```

Then, to finally send it to the docker container, type the following command : 
```sh
docker cp <jar_file_path>/<name_of_your_jar_file> hadoop-master:/root/<name_of_your_jar_file>
```

For instance, if I want to push the jar file of the salesCount project, I'd go to its root directory and type `docker cp target/salescount-1.jar hadoop-master:/root/salescount-1.jar`

### Execute the jars inside the master container
#### Salescount
The salescount project is a pure hadoop project. To use it, type the following command inside the master container's bash : 
```sh
hadoop jar salescount-1.jar main.java.hadoop.mapreduce.SalesCount input output
```

Where :
* `salescount-1.jar` is the name of the jar file we previously pushed into the container
* `main.java.hadoop.mapreduce.` is the package of the main class
* `SalesCount` is the main class
* `input` and `output` are parameters


#### Wordcount
The word count project uses spark batch. To use it, type the following command inside the master container's bash :
```sh
spark-submit --class spark.batch.WordCountTask --master local --driver-memory 4g --executor-memory 2g --executor-cores 1 wordcount-1.jar input/purchases.txt output
```


#### Stream
The stream project is the same as the wordcount project, but instead of analysing data from a text file, it actually listens to a data stream that you can contole.
To use it, type the following command inside the master container's bash :
```sh
spark-submit --class spark.streaming.Stream --master local --driver-memory 4g --executor-memory 2g --executor-cores 1 stream-1.jar
```

Then, on your host, type `nc -lk 9999` and start typing messages!

#### Processing
The processing project takes a peek at the way one can use Spark to process data from a NoSql database, in this case HBase.

> Note : this project requires a bit more time to set up than the other ones.

Start your containers using this `docker-compose up -d` command.

Once your containers are up and running, enter the master container's bash, and execute its setup commands (all of them are show down below).

```sh
# ================ In the hadoop-master container's bash ================
# Start daemons
start-hadoop.sh
start-hbase.sh

# Setup datasource
hadoop fs -mkdir -p input # if the input directory is already present, no need to execute this line.
hadoop fr -put purchases2.txt input

# Setup NoSql database
hbase shell
    # Inside the newly opened HBase shell
    create 'products','cf'
    exit

# Back to the container's bash
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=',' -Dimporttsv.columns=HBASE_ROW_KEY,cf:date,cf:time,cf:town,cf:product,cf:price,cf:payment products input

# To make sure the base was properly created
hbase shell
    get 'products','2000',{COLUMN => 'cf:town'}
    exit
```

Once this is done, you can compile the `processing` project and send the jar to the container using the following command : 

```sh
# ================ In your host ================
cd processing
mvn package
docker cp target/processing-1.jar hadoop-master:/root/processing-1.jar
```

We're getting there! Now that your container has the jar file, copy the HBase libraries inside Spark's jars folder, and launch the jar !
```sh
# ================ In the hadoop-master container's bash ================
cp -r $HBASE_HOME/lib/* $SPARK_HOME/jars
spark-submit  --class hbase.spark.HbaseSparkProcess --master yarn --deploy-mode client processing-1.jar
```
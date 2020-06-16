package spark.batch;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class WordCountTask {

    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please provide the paths of input file and output dir.");
        }

        final String inputFilePath = args[0];
        final String outputDirPath = args[1];

        SparkConf conf = new SparkConf()
            .setMaster("local")
            .setAppName("Spark word count");
        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> textFile = context.textFile(inputFilePath);
        JavaPairRDD<String, Integer> counts = textFile
            .flatMap(str -> Arrays.asList(str.split("\t")).iterator())
            .mapToPair(word -> new Tuple2<>(word, 1))
            .reduceByKey((a, b) -> a + b);

        counts.saveAsTextFile(outputDirPath);
        context.close();
    }
}
package spark.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

public class Stream {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf()
            .setAppName("Network Word Count")
            .setMaster("local[*]");

        JavaStreamingContext context = new JavaStreamingContext(conf, Durations.seconds(1));

        JavaReceiverInputDStream<String> inputtedLines = context.socketTextStream(
            args.length > 0 && args[0].length() > 0 ? args[0] : HOSTNAME,
            args.length > 1 && args[1].length() > 0 ? Integer.parseInt(args[1]) : PORT,

        );

        JavaDStream<String> words = inputtedLines.flatMap(
            line -> Arrays.asList(line.split(" ")).iterator()
        );

        JavaPairDStream<String, Integer> pairs = words.mapToPair(
            word -> new Tuple2<>(word, 1)
        );

        JavaPairDStream<String, Integer> wordCount = pairs.reduceByKey((i1, i2) -> i1 + i2);

        wordCount.print();
        context.start();
        context.awaitTermination();
    }
  }
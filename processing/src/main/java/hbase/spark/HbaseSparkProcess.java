package hbase.spark;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class HbaseSparkProcess {
    public void createHbaseTable() {
        Configuration conf = HBaseConfiguration.create();
        SparkConf sparkConf = new SparkConf()
            .setAppName("SparkHBaseTest")
            .setMaster("local[4]");
        JavaSparkContext context = new JavaSparkContext(sparkConf);

        conf.set(TableInputFormat.INPUT_TABLE, "products");

        JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = 
            context.newAPIHadoopRDD(
                conf,
                TableInputFormat.class,
                ImmutableBytesWritable.class,
                Result.class
            );

        System.out.println("Nombre d'enregistrements : " + hBaseRDD.count());
        context.close();
    }

    public static void main(String[] args) {
        HbaseSparkProcess admin = new HbaseSparkProcess();
        admin.createHbaseTable();
    }
}
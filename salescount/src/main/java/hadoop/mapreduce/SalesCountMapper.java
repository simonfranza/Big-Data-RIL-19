package main.java.hadoop.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SalesCountMapper extends Mapper<Object, Text, Text, IntWritable> {

  private final static IntWritable one = new IntWritable(1);
  private Text store = new Text();

  public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    String[] fields = value.toString().split("\t");

    if (null != fields && fields.length > 2 && fields[SalesColumns.STORE].length() > 0) {
      store.set(fields[SalesColumns.STORE]);
      context.write(store, one);
    }
  }
}
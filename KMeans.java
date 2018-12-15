import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.io.*;
import java.util.*;
import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;

public class KMeans {

	public static List<Double> Centers = new ArrayList<Double>(); //形心位置
	public static boolean isdone = false, t = false; //判斷是否繼續執行
	public static String out; //輸出結果
	public static String in; //輸入資料
	
	public static class Map extends Mapper<Object, Text, DoubleWritable, DoubleWritable>{  //Input Key, Input Value, Output Key, Output Value
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
if (t == false) { //設定起始形心位置
Centers.add(Double.parseDouble("20"));
Centers.add(Double.parseDouble("30"));
Centers.add(Double.parseDouble("40"));
t = true;
}

			String inputvalue = value.toString(); //Input Value讀取
			double point = Double.parseDouble(inputvalue); //轉換成double型態
			double a, b = Double.MAX_VALUE, nearest_center = Centers.get(0); //設定比較參數及選定最近形心參數

			// 找出最近形心
			for (double c : Centers) {
				a = c - point;
				if (Math.abs(a) < Math.abs(b)) {
					nearest_center = c;
					b = a;
				}
			}
			// 送出最近形心與相對應點
			
			context.write(new DoubleWritable(nearest_center), new DoubleWritable(point));
			
		}
  }

  public static class Reduce extends Reducer<DoubleWritable, DoubleWritable, DoubleWritable, Text> { //Input Key, Input Value, Output Key, Output Value
	
    public void reduce(DoubleWritable key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
			double newCenters; //新的形心參數
			double sum = 0; //所有點的總和
			int tatal = 0; //距離某一形心總數
			String points = "";
			Centers.clear(); //清理原本的形心

			for (DoubleWritable valuess : values){ //印出所屬形心的集合點
				double d = valuess.get();
				points = points + " " + Double.toString(d);
				sum = sum + d;
				tatal++;
			}

			
			newCenters = sum / tatal; //更新新的形心，利用平均

			Centers.add(newCenters);//放入新的形心

			double temp = key.get();
			if(Math.abs( temp - newCenters) <= 0.1) { //判斷前後形心是否小於0.1
				isdone = true;
					}
			else {
				isdone = false;
				}

			// 送出最近形心與相對應點
			context.write(new DoubleWritable(newCenters), new Text(points));
		}
  }

  public static void main(String[] args) throws Exception {
		

		while(isdone == false){
		in = args[0];
		out = args[1];
		String input = in;
		String output = out;
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "KMeans");
    job.setJarByClass(KMeans.class);
    //job.setInputFormatClass (LongWritable.class);
    job.setMapOutputKeyClass(DoubleWritable.class);
    job.setMapOutputValueClass(DoubleWritable.class);
    
    job.setOutputKeyClass(DoubleWritable.class);
    job.setOutputValueClass(Text.class);
    job.setMapperClass(Map.class);
    //job.setCombinerClass(Reduce.class);
    job.setReducerClass(Reduce.class);
    
    FileInputFormat.addInputPath(job, new Path(input));
    FileOutputFormat.setOutputPath(job, new Path(output));
	
    System.exit(job.waitForCompletion(true) ? 0 : 1);
			}
  }
}

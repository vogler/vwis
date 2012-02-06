package de.tum.in.vwis.e13;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;

public class Join {

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable position, Text line,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
//			Path path = ((FileSplit)reporter.getInputSplit()).getPath();
			StringTokenizer tok = new StringTokenizer(line.toString(), "|");
			Text key = new Text(tok.nextToken().trim());
			Text payload = new Text(tok.nextToken().trim());
			output.collect(key, payload);
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			List<String> payloads = new ArrayList<String>();
			while (values.hasNext() && payloads.size() < 2) {
				payloads.add(values.next().toString());
			}
			if(payloads.size() < 2){
				System.out.println("Size for key "+key.toString()+": "+payloads.size());
			}else{
				output.collect(key, new Text(Arrays.toString(payloads.toArray())));
			}
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Join.class);
		conf.setJobName("join");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(IdentityReducer.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}

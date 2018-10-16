package com.spark.streaming.tools.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.streaming.tools.model.MessageModel;
import com.spark.streaming.tools.model.RecordModel;
import com.spark.streaming.tools.utils.StreamingConfig;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Write code here that does processes streaming data
 */
public class ProcessStreamingData<K, M, U> implements Serializable, Function<JavaPairRDD<K, M>, Void> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessStreamingData.class);

    private final StreamingConfig config;
    final ObjectMapper mapper = new ObjectMapper();

    private static AtomicLong orderCount = new AtomicLong(0);

    public ProcessStreamingData(StreamingConfig config) {
        this.config = config;
    }

    /*
     * 1. Each RDD is split into multiple partitions for better parallel processing
     * 2. Each tuple in in each partition is processed.
     * 3. send back to hive
     */
    public void execute(JavaPairRDD<String, byte[]> inputMessage) {
        JavaPairRDD<String, byte[]> partitionedRDD;
        if (config.getLocalMode())
            partitionedRDD = inputMessage;
        else {
            // Helps scale beyond number of input partitions in kafka
            partitionedRDD = inputMessage.repartition(config.getRepartitionCount());

        }

        //change data format
        JavaRDD<MessageModel> msgRDD = partitionedRDD.map(
                new Function<Tuple2<String, byte[]>, MessageModel>() {
                    @Override
                    public MessageModel call(Tuple2<String, byte[]> t2) throws Exception {
                        MessageModel m = mapper.readValue(t2._2, MessageModel.class);
                        return m;
                    }
                }
        );
        //fact table
        JavaRDD<RecordModel> recordRDD = msgRDD.flatMap(
                new FlatMapFunction<MessageModel, RecordModel>() {
                    @Override
                    public Iterable<RecordModel> call(MessageModel messageModel) throws Exception {
                        List<RecordModel> m = new ArrayList<RecordModel>();
                        for (MessageModel.Metric metric : messageModel.metrics) {
                            MessageModel.Dimension d = messageModel.dimensions;
                            m.add(new RecordModel(metric.values, metric.name, metric.timestamp, d.datacenter, d.application, d.host));
                        }
                        return (Iterable<RecordModel>) m.iterator();
                    }
                }

        ).cache();


        // calculate dimension table, eq: searchCount
        JavaPairRDD<Tuple2<String, String>, Double> searchCountRDD = recordRDD.filter(r -> r.name.equals("searchCount") ? true : false).mapToPair(x -> new Tuple2<>(new Tuple2<>(x.application, x.datacenter), x.values)).reduceByKey((i1, i2) -> i1 + i2);


    }

    @Override
    public Void call(JavaPairRDD<K, M> kmJavaPairRDD) throws Exception {
        execute((JavaPairRDD<String, byte[]>) kmJavaPairRDD);
        return null;
    }

}




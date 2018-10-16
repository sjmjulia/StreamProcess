package com.spark.streaming.tools.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * message for input data.
 */
public class MessageModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public class Metric implements Serializable{
        public Double values;
        public String name;
        public String timestamp;

        public Metric(Double values, String name, String timestamp) {
            this.values = values;
            this.name = name;
            this.timestamp = timestamp;
        }

        public Metric() {}
    }

    public class Dimension implements Serializable{
        public String datacenter;
        public String application;
        public String host;

        public Dimension(String datacenter, String application, String host) {
            this.datacenter = datacenter;
            this.application = application;
            this.host = host;
        }

        public Dimension() {
        }
    }

    public List<Metric> metrics;
    public Dimension dimensions;

    public MessageModel(List<Metric> metrics, Dimension dimensions) {
        this.metrics = metrics;
        this.dimensions = dimensions;
    }

    public MessageModel(){
        metrics = new ArrayList<>();
        dimensions = new Dimension();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public Dimension getDimensions() {
        return dimensions;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public void setDimensions(Dimension dimensions) {
        this.dimensions = dimensions;
    }
}

package com.spark.streaming.tools.model;

import java.io.Serializable;

/**
 * fact table
 */
public class RecordModel  implements Serializable {
    public Double values;
    public String name;
    public String timestamp;
    public String datacenter;
    public String application;
    public String host;

    public RecordModel(Double values, String name, String timestamp, String datacenter, String application, String host) {
        this.values = values;
        this.name = name;
        this.timestamp = timestamp;
        this.datacenter = datacenter;
        this.application = application;
        this.host = host;
    }
}

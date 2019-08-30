# Producer dashboard
## References 
* [Datadog](https://www.datadoghq.com/blog/monitoring-kafka-performance-metrics/)
* [Documentation](https://kafka.apache.org/documentation/#producer_monitoring)
## Metrics 
JMX  
```
kafka.producer:type=producer-metrics,client-id="{client-id}"
kafka.producer:type=producer-node-metrics,client-id=([-.\w]+),node-id=([0-9]+)
kafka.producer:type=producer-topic-metrics,client-id="{client-id}"
```

## I/O
| Metric   |      Prometheus format      |  Description 1 |
|:----------|:-------------|:------|
| io-ratio |  producer_metrics_io_ratio | Fraction of time I/O thread spent doing I/O. Average length of time the I/O thread spent doing for a socket (`in ns`)|
| io-wait-ratio |  producer_metrics_io_wait_ration | Fraction of time I/O thread spent waiting I/O | Average length of time the I/O thread spent waiting for a socket (in ns)|
| user-processing |  1 - producer_metrics_io_ratio - producer_metrics_io_wait_ration | User processing ratio | 
| outgoing-byte-rate |  producer_metrics_outgoing_byte_rate | The average number of outgoing bytes sent per second to all servers. | 
 
If user processing time is high, the single Producer I/O thread may be busy

Producers generally do one of two things: wait for data, and send data. 
If producers are producing more data than they can send, they end up waiting for network resources. 
But if producers aren’t being rate-limited or maxing-out their bandwidth, the bottleneck becomes harder to identify. 
Because disk access tends to be the slowest segment of any processing task, checking I/O wait times on your producers is a good place to start. 
Remember, I/O wait represents the percent of time spent performing I/O while the CPU was idle. 
If you are seeing excessive wait times, it means your producers can’t get the data they need fast enough. 
If you are using traditional hard drives for your storage backend, you may want to consider SSDs.

`Outgoing byte rate`: As with Kafka brokers, you will want to monitor your producer network throughput. 
Observing traffic volume over time is essential to determine if changes to your network infrastructure are needed. 
Additionally, you want to be sure that your producers are sending information at a constant rate for consumers to ingest. 
Monitoring producer network traffic will help to inform decisions on infrastructure changes, as well as to provide a window into the production rate of producers and identify sources of excessive traffic.

## Request Response
| Metric   |      Prometheus format      |  Description | Value|
|:----------|:-------------|:------|:-------|
| response-rate |  producer_metrics_response_rate | Responses received per second. | #responses / sec|
| request-rate |  producer_metrics_request_rate | The average number of requests sent per second. | avg(#requests) / sec |
| request-latency-avg |  producer_metrics_request_latency_avg | The average request latency in ms for a node. | avg(#requests_latency) in milliseconds |

`NB!`: Pay attention that response rate is absolute number, but request-rate is avg.

`Response rate`: For producers, the response rate represents the rate of responses received from brokers. Brokers respond to producers when the data has been received. 
Depending on your configuration, “received” could mean a couple of things:
The message was received, but not committed (request.required.acks == 0)
The leader has written the message to disk (request.required.acks == 1)
The leader has received confirmation from all replicas that the data has been written to disk (request.required.acks == -1)
This may come as a surprise to some readers, but producer data is not available for consumption until the required number of acknowledgments have been received.
If you are seeing low response rates, a number of factors could be at play. 
A good place to start is by checking the request.required.acks configuration directive on your brokers. 
Choosing the right value for request.required.acks is entirely use case dependent—it’s up to you whether you want to trade availability for consistency.

`Request rate`: The request rate is the rate at which producers send data to brokers. 
Of course, what constitutes a healthy request rate will vary drastically depending on the use case. 
Keeping an eye on peaks and drops is essential to ensure continuous service availability. 
If rate-limiting is not enabled (version 0.9+), in the event of a traffic spike brokers could slow to a crawl as they struggle to process a rapid influx of data.

`Request latency average`: The average request latency is a measure of the amount of time between when KafkaProducer.send() was called until the producer receives a response from the broker. 
“Received” in this context can mean a number of things, as explained in the paragraph on response rate.
There are a number of ways by which you can reduce latency. The main knob to turn is the producer’s linger.ms configuration. 
his setting tells the producer how long it will wait before sending, in order to allow messages to accumulate in the current batch. 
By default, producers will send all messages immediately, as it gets an ack from the last send. 
However, not all use cases are alike, and in many cases, waiting a little longer for message accumulation results in higher throughput.
Since latency has a strong correlation with throughput, it is worth mentioning that modifying batch.size in your producer configuration can lead to significant gains in throughput. 
There is no “one size fits all” when it comes to appropriate batch size; determining an optimal batch size is largely use case dependent. 
A general rule of thumb is that if you have the memory, you should increase batch size. 
Keep in mind that the batch size you configure is an upper limit, meaning that Kafka won’t wait forever for enough data before it sends, it’ll only wait a maximum of linger.ms milliseconds. 
Small batches involve more network round trips, and result in reduced throughput, all other things equal.


todo:
* batch-size-avg
* compression-rate-avg

* byte-rate
* record-send-rate
* record-error-rate


# Producer dashboard
## Metrics 
[JMX] - `kafka.producer:type=producer-metrics,client-id="{client-id}"`

## I/O
| Metric   |      Prometheus format      |  Description 1 | Description 2 |
|:----------|:-------------|:------|:------|
| io-ratio |  producer_metrics_io_ratio | fraction of time I/O thread spent doing I/O | Average length of time the I/O thread spent doing for a socket (`in ns`)|
| io-wait-ratio |  producer_metrics_io_wait_ration | fraction of time I/O thread spent waiting I/O | Average length of time the I/O thread spent waiting for a socket (in ns)|
| user-processing |  1 - producer_metrics_io_ratio - producer_metrics_io_wait_ration | User processing ratio | |
 
If user processing time is high, the single Producer I/O thread may be busy


[Datadog](https://www.datadoghq.com/blog/monitoring-kafka-performance-metrics/)
Producers generally do one of two things: wait for data, and send data. 
If producers are producing more data than they can send, they end up waiting for network resources. 
But if producers aren’t being rate-limited or maxing-out their bandwidth, the bottleneck becomes harder to identify. 
Because disk access tends to be the slowest segment of any processing task, checking I/O wait times on your producers is a good place to start. 
Remember, I/O wait represents the percent of time spent performing I/O while the CPU was idle. 
If you are seeing excessive wait times, it means your producers can’t get the data they need fast enough. 
If you are using traditional hard drives for your storage backend, you may want to consider SSDs.

## Req/Res rate
| Metric   |      Prometheus format      |  Description |
|:----------|:-------------|:------|
| response-rate |  producer_metrics_response_rate | Responses received per second. |
| request-rate |  producer_metrics_request_rate | The average number of requests sent per second. |

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




##metricd

Linux system monitoring agent written in Java. Also comes with a Jetty server and a dashboard uses d3.js. First version of this project was created as a closed source project at August 2014, only supports to send data to Riemann and dashboard not included.

This project aims to collect system related metrics in < 10ms with a very low CPU overhead in a low-end box using 50-100MB memory.

Currently implemented readers

* CPU
* Memory
* Swap
* Disk usage (currently root filesystem)
* IO stats (currently only sda)
* Network statistics (currently only supports eth0)
* Load averages
* Network connections

Metric readers for SQL servers, web servers and popular tools will be added.

Only supports writing metrics to stdout. But stable version aims to provide sending data to Riemann, InfluxDB, Graphite, StatsD, JSON HTTP and more..

**This is a pre-release and things will be change before stable release**


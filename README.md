##metricd

Linux system monitoring agent written in Java. Also comes with a Jetty server and a dashboard uses d3.js. First version of this project was created as a closed source project at August 2014, only supports to send data to Riemann and dashboard not included.

This project aims to collect system related metrics in < 10ms with a very low CPU overhead in a low-end box using 50-100MB memory.

Currently implemented readers

* CPU
* Memory
* Swap
* Disk usage
* IO stats
* Network statistics (only supports eth0)
* Load averages
* Network connections

Currently implemented writers
* Riemann
* Console
* Jetty JSON
* HTTP JSON

**This is a highly experimental project and things will be change before stable release**


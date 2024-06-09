# BATCH PIPEPILINE 
## @Author: Dissan Uddin Ahmed
### Project 1 for [SABD2324] 

### The java application code is in spark/spark-batch-queries
### Dockerfile for Hdfs is in hadoop
### Dockerfile for spark is in spark
### Nifi flow-files are in nifi/flow-files


### Build dockerfiles hadoop-cluster and spark-cluster

```
    source run.sh --install
```

### Start docker compose file
```
    source run.sh --start
```
### Build jar files in the folder spark/spark-batch-queries/
#### Requirements maven and java 8
```
    source run.sh --jars
```

### Init Hadoop Directories
```
    source run.sh --init
```

### Run first query 
```
    source run.sh --query
```

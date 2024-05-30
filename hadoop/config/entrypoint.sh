#!/bin/bash

start_namenode() {
  if [ ! -d "/hadoop/logs/current" ]; then
    echo "Formatting NameNode..."
    $HADOOP_HOME/bin/hdfs namenode -format -force -nonInteractive
  fi
  echo "Starting HDFS NameNode..."
  $HADOOP_HOME/bin/hdfs --daemon start namenode
  tail -f $HADOOP_HOME/logs/hadoop.log
}

start_datanode() {
  echo "Starting HDFS DataNode..."
  $HADOOP_HOME/bin/hdfs --daemon start datanode
  tail -f $HADOOP_HOME/logs/hadoop-*-datanode-*.out
}

start_resourcemanager() {
  echo "Starting YARN ResourceManager..."
  $HADOOP_HOME/bin/yarn --daemon start resourcemanager
  tail -f $HADOOP_HOME/logs/hadoop-*-resourcemanager-*.out
}

start_nodemanager() {
  echo "Starting YARN NodeManager..."
  $HADOOP_HOME/bin/yarn --daemon start nodemanager
  tail -f $HADOOP_HOME/logs/hadoop-*-nodemanager-*.out
}

case "$1" in
  namenode)
    start_namenode
    ;;
  datanode)
    start_datanode
    ;;
  resourcemanager)
    start_resourcemanager
    ;;
  nodemanager)
    start_nodemanager
    ;;
  *)
    echo "Usage: $0 {namenode|datanode|resourcemanager|nodemanager}"
    exit 1
esac

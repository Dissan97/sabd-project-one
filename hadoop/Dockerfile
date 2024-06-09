FROM ubuntu:latest
USER root

RUN apt-get update
RUN apt-get install -y curl tar sudo openssh-server rsync openjdk-8-jre-headless vim net-tools

# passwordless ssh
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa 
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# # hadoop
RUN wget https://downloads.apache.org/hadoop/common/hadoop-3.4.0/hadoop-3.4.0.tar.gz ; tar -zxf hadoop-3.4.0.tar.gz -C /usr/local/ ; rm hadoop-3.4.0.tar.gz
RUN cd /usr/local && ln -s ./hadoop-3.4.0 hadoop
RUN mkdir /hadoop && mkdir /hadoop/logs
# # java
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64
ENV PATH $PATH:$JAVA_HOME/bin
# 
ENV HADOOP_COMMON_HOME /usr/local/hadoop
ENV HADOOP_HDFS_HOME /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV HADOOP_MAPRED_HOME /usr/local/hadoop
ENV HADOOP_YARN_HOME /usr/local/hadoop
ENV HADOOP_CONF_DIR /usr/local/hadoop/etc/hadoop
ENV YARN_CONF_DIR $HADOOP_HOME/etc/hadoop
ENV PATH $PATH:$HADOOP_HOME/bin
ENV HDFS_NAMENODE_USER "root"
ENV HDFS_DATANODE_USER "root"
ENV HDFS_SECONDARYNAMENODE_USER "root"
ENV YARN_RESOURCEMANAGER_USER "root"
ENV YARN_NODEMANAGER_USER "root"
ENV HDFS_NAMENODE_OPTS="-XX:+UseParallelGC -Xmx4g"

#ADD config/hadoop-env.sh $HADOOP_HOME/etc/hadoop/hadoop-env.sh
ADD config/core-site.xml $HADOOP_HOME/etc/hadoop/core-site.xml
ADD config/hdfs-site.xml $HADOOP_HOME/etc/hadoop/hdfs-site.xml
ADD config/yarn-site.xml $HADOOP_HOME/etc/hadoop/yarn-site.xml
ADD config/mapred-site.xml $HADOOP_HOME/etc/hadoop/mapred-site.xml
ADD config/log4j.properties $HADOOP_HOME/etc/hadoop/log4j.properties
ADD config/workers $HADOOP_HOME/etc/hadoop/workers
# 

# 
ADD config/ssh_config /root/.ssh/config
RUN chmod 600 /root/.ssh/config
RUN chown root:root /root/.ssh/config
RUN mkdir -p /nodemanager/logs && mkdir -p /nodemanager/intermediate
# 

WORKDIR ${HADOOP_HOME}
COPY config/entrypoint.sh .
COPY config/setup_hdfs.sh .
# Expose necessary ports based on the configuration
# Ports from core-site.xml
# Ports from yarn-site.xml
EXPOSE 4040 4041 4042 4043 8040 8088 9870 54310
# Use the entrypoint script

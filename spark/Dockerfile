FROM hadoop-cluster
USER root

RUN wget https://dlcdn.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz ; tar -zxf spark-3.5.1-bin-hadoop3.tgz -C /usr/local/ ; rm spark-3.5.1-bin-hadoop3.tgz
RUN cd /usr/local && ln -s ./spark-3.5.1-bin-hadoop3 spark && sudo chmod +x /usr/local/spark/bin/*
RUN mkdir -p /spark/pid && mkdir -p /spark/history-logs

ENV SPARK_HOME /usr/local/spark
ENV PATH=$PATH:${SPARK_HOME}/bin
ENV SPARK_CONF_DIR ${SPARK_HOME}/conf
ENV SPARK_LOG_DIR ${SPARK_HOME}/logs
ENV SPARK_LOG_MAX_FILES 5
ENV SPARK_PID_DIR /spark/pid
ENV PATH=$PATH:/usr/local/spark/bin
ENV LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH
ENV HADOOP_USER_NAME=spark
ENV PATH=$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH
COPY conf/spark-defaults.conf ${SPARK_HOME}/conf
WORKDIR /usr/local/spark


#CMD [ "pyspark" ]

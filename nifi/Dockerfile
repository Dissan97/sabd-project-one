FROM hadoop-cluster
USER root

RUN wget https://dlcdn.apache.org/zookeeper/zookeeper-3.9.2/apache-zookeeper-3.9.2-bin.tar.gz ; tar -zxf apache-zookeeper-3.9.2-bin.tar.gz -C /usr/local/ ; rm apache-zookeeper-3.9.2-bin.tar.gz
RUN cd /usr/local && ln -s ./apache-zookeeper-3.9.2-bin zookeeper && sudo chmod +x /usr/local/zookeeper/bin/* && mkdir -p /zookeeper/data
RUN apt update && apt install -y unzip
RUN wget https://dlcdn.apache.org/nifi/1.26.0/nifi-1.26.0-bin.zip ; unzip nifi-1.26.0-bin.zip -d /tmp/ ; rm nifi-1.26.0-bin.zip

RUN mkdir /usr/local/nifi && mv /tmp/nifi-1.26.0/* /usr/local/nifi && rmdir /tmp/nifi-1.26.0 && mkdir -p /nifi/data

COPY conf/zoo.cfg /usr/local/zookeeper/conf
ENV NIFI_HOME /usr/local/nifi
ENV PATH=$PATH:${NIFI_HOME}/bin

#RUN cd /usr/local && mkdir nifi && mv temp/nifi-2.0.0-M3/* nifi && rmdir temp
WORKDIR ${NIFI_HOME}
#RUN mvn clean install -Pinclude-all
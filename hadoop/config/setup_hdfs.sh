set -e
echo "setup the directories in hdfs"
hdfs dfs -mkdir -p /user/spark 
hdfs dfs -chown spark:hadoop /user/spark 
hdfs dfs -chmod 0775 /user/spark
hdfs dfs -mkdir -p /user/spark/logs/spark-event-logs
hdfs dfs -chmod 0775 /user/spark/logs/spark-event-logs
echo "creating directory in hdfs"
hdfs dfs -mkdir -p /user/spark/pipeline/source
hdfs dfs -chown spark:hadoop /user/spark/pipeline
hdfs dfs -chown spark:hadoop /user/spark/pipeline/source
hdfs dfs -chmod 0777 /user/spark/pipeline/source
hdfs dfs -mkdir -p /user/spark/pipeline/preprocessed
hdfs dfs -chown nifi:hadoop /user/spark/pipeline/preprocessed
hdfs dfs -chmod 0777 /user/spark/pipeline/preprocessed
hdfs dfs -mkdir -p /user/spark/pipeline/jars
hdfs dfs -chown spark:hadoop /user/spark/pipeline/jars
hdfs dfs -chmod 0777 /user/spark/pipeline/jars
echo "directories created"


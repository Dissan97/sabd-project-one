set -e
echo "creating directory in hdfs"
hdfs dfs -mkdir -p /pipeline/source
echo "directories created"
hdfs dfs -ls /
hdfs dfs -ls /pipeline
echo "changing permission for /pipeline"
hdfs dfs -chmod 0777 /pipeline
echo "changing permission for /pipeline/source"
hdfs dfs -chmod 0777 /pipeline/source
echo "loading data.csv to hdfs"
hdfs dfs -put /pipeline/source/data.csv /pipeline/source
echo "Data.csv loaded"
echo "changing permission to data.csv"
hdfs dfs -chmod 0777 /pipeline/source/data.csv

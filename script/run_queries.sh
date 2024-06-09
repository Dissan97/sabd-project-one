echo RUNNING CSV 
echo QUERY ONE
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryOne /app/Spark-queries.jar $1 csv
echo QUERY TWO
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryTwo /app/Spark-queries.jar $1 csv
echo QUERY THREE
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryThree /app/Spark-queries.jar $1 csv
echo RUNNING PARQUET
echo QUERY ONE
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryOne /app/Spark-queries.jar $1 parquet
echo QUERY TWO
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryTwo /app/Spark-queries.jar $1 parquet
echo QUERY ONE
docker compose exec spark spark-submit --master spark://spark:7077 --class dissanuddinahmed.QueryThree /app/Spark-queries.jar $1 parquet



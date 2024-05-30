ARGS ?= dataframe
LINES ?= echo "---------------------------"


install:
	docker build -t hadoop-cluster hadoop
	docker build -t spark-cluster spark

package-java:
		$(LINES)
		echo "PACKAGING spark-batch-queries"
		$(LINES)
		mvn -f spark/Spark-batch-queries/ package
		$(LINES)
		echo "PACKAGING spark-batch-queries-sql"
		$(LINES)
		mvn -f spark/spark-batch-queries-sql/ package
		$(LINES)
		echo "COMPLETED package-java"
		$(LINES)

start:
	docker compose up -d

remove:
	docker compose down

load_file:
	docker compose exec namenode bash ./load_data_set.sh

start_queries:
	cp spark/Spark-batch-queries/app/Spark-queries.jar app/
	docker compose exec spark spark-submit /app/Spark-queries.jar $(ARGS)


execute_all: install start load_file start_queries

echo_test:
	echo $(ARGS)

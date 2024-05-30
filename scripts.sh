lines="-------------------------------------------"

install_images (){
	echo -e "$lines\nbuilding images\n$lines"
	echo -e "building hadoop-cluster\n$lines"
	docker build hadoop -t hadoop-cluster
	echo -e "$lines/ndone....\n$lines"
	echo -e "building spark-cluster\n$lines"
	docker build spark -t spark-cluster
	echo -e "$lines\ndone....\n$lines"
	echo -e "pulling apache/nifi:latest\n$lines"
	docker pull apache/nifi:latest
	echo -e "setup completed\n$lines"
}

install_jars(){
	echo -e "$lines\nbuilding spark-batch-queries package\n$lines"
	mvn -f spark/spark-batch-queries/ package
	echo -e "$lines\nbuilding spark-batch-queries-sql package\n$lines"
	mvn -f spark/spark-batch-queries-sql/ package
	echo -e "$lines\nbuilded all the jars\n$lines"
}

run_compose(){
	echo -e "$lines\nlaunchig docker-compose\n$lines"
	docker compose up -d
	echo -e "$lines\ndone\n$lines"
}

load_dataset(){

	if [ -n "$2"]; then 
		cp "$2" data-stored/data.csv
		if [ $? -eq 0 ]; then
    			echo "File copied and renamed to data.csv successfully."
		else
    			echo "Error: Failed to copy and rename the file."
    			exit 1
		fi
	fi
	docker compose exec namenode bash ./load_data_set.sh

}

launch_spark_queries(){
	echo -e "$lines\nlaunching Spark-queries.jar on container spark\n$lines"
	echo -e "$lines\ncan observer on port spark:4000"
	docker compose exec spark spark-submit /app/Spark-queries.jar
	echo -e "$lines\ndone\n$line"
}

launch_spark_queries_sql(){
	echo -e "$lines\nlaunching Spark-queries-sql.jar on container spark\n$lines"
	echo -e "$lines\ncan observer on port spark:4000"
	docker compose exec spark spark-submit /app/Spark-queires-sql.jar
	echo -e "$lines\ndone\n$line"
}	

run_queries(){
	echo -e "executing all the jars\n$line"
	launch_spark_queries
	launch_spark_queries_sql
}

run_query(){
	case "$2" in
		dataframe)
		launch_spark_queries
		;;
		sql)
		launch_spark_queries_sql
		;;
		help|h|man)
		cat script-man/run_query
		;;
		*)
		echo "$2 not valid arg launch $1 help to check help"
		exit 1
		
	esac
}

close_compose(){
	docker compose down
}

case "$1" in
	install-images)
	install_images
	;;
	install-jars)
	install_jars
	;;
	load-dataset)
	load_dataset
	;;
	run-queries)
	run_queries
	;;
	run-query)
	run_query
	;;
	install-images)
	install_images
	;;
	run-compose)
	run_compose
	;;
	install-jars)
	install_jars
	;;
	load-dataset)
	load_dataset
	;;
	run-queries)
	run_queries
	;;
	run-query)
	run_query
	;;
	""|all)
	install_images
	install_jars
	load_data_set
	run_queries
	;;
	help|h|man)
	cat script-man/scripts
	;;
	close|close-compose)
	close_compose
	;;
	*)
	echo -e "Usage: $0 {install-images|install-jars|run-queries|load-dataset|run-query{dataframe|sql}|all}\nfor all the args type help for info"
	#exit 0
esac


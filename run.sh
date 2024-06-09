lines="-------------------------------------------"
set -e
install_images (){
	echo -e "$lines\nbuilding images\n$lines"
	echo -e "building hadoop-cluster\n$lines"
	docker build hadoop -t hadoop-cluster
	echo -e "$lines/ndone....\n$lines"
	echo -e "building spark-cluster\n$lines"
	docker build spark -t spark-cluster
	echo -e "$lines\ndone....\n$lines"
}

start_compose(){
	echo -e "$lines\nrunning docker compose\n$lines"
	if [ -z "$1" ]; then
	docker compose up --remove-orphans -d 
	else
		if [ "$1" == "v" ]; then
		echo -e "$lines\nvertical\n$lines"
			case "$2" in
				3)
					echo -e "$lines\n$2\n$lines"
				;;
				5)
					echo -e "$lines\n$2\n$lines"
				;;
				*)
					echo -e "$lines\ninvalid argument pass v 3|5\n$lines"
				;;
			esac
		elif  [ "$1" == "h" ]; then
			echo -e "$lines\nhorizontal\n$lines"
			case "$2" in
				3)
					docker compose up -d --scale spark-worker=3
				;;
				5)
					docker compose up -d --scale spark-worker=5
				;;
				*)
					echo -e "$lines\ninvalid argument pass v 3|5\n$lines"
				;;
			esac
		elif [ "$1" == "help" ]; then
		echo -e "$lines\nhelp\n$lines"		
		else
		echo -e "$lines\ninvalid argument check --start help\n$lines"
		fi
	fi
	
}
install_jars(){
	echo -e "$lines\nbuilding spark-batch-queries package\n$lines"
	mvn -f spark/spark-batch-queries/ package
	echo -e "$lines\nbuilding spark-batch-queries-sql package\n$lines"
	mvn -f spark/spark-batch-queries-sql/ package
	echo -e "$lines\nbuilded all the jars\n$lines"
}

init_hdfs(){
	echo -e "$lines\nrunning setup_hdfs in namenode\n$lines"
	docker compose exec namenode bash setup_hdfs.sh
}

run_query(){
	echo -e "$lines\nlaunching queries\n$lines"
	if [ -z "$1" ]; then
	source script/run_queries.sh 1W-1C-1G
	else
		case "$1" in 
		h3)
		docker compose up -d --remove-orphans --scale spark-worker=3
		source script/run_queries.sh 3W-1C-1G
		;;
		h5)
		docker compose up -d --remove-orphans --scale spark-worker=5
		source script/run_queries.sh 5W-1C-1G
		;;
		v3)
		docker compose -f docker-compose-vertical-3.yaml up --remove-orphans -d
		source script/run_queries.sh 1W-5C-5G
		;;
		v5)
		docker compose -f docker-compose-vertical-3.yaml up --remove-orphans -d
		source script/run_queries.sh 1W-5C-5G		
		;;		
		*)
		echo -e "$lines\ninvalid argument pass ''|h3|h5|v3|v5\n$lines"
		;;
		esac
	fi
}

case "$1" in
	--install)
	install_images
	;;
	--start)
	start_compose $2 $3
	;;
	--jars)
	install_jars
	;;
	--init)
	init_hdfs
	;;
	--query)
	run_query $2 $3
	;;
	--help|--h|--man)
	cat script-man/man_run
	;;
	*)
	echo -e "Usage: $0 {--install|--start|--jars|--init|--query}\nfor all the args type --help or -h for info"
	;;
esac


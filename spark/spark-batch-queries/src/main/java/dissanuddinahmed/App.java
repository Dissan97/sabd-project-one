package dissanuddinahmed;

import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Locale;
import java.util.logging.Logger;

public class App {

	protected static final String APP_NAME = "[SABD2324] Project 1";
	protected static final Logger LOGGER = Logger.getLogger(APP_NAME);
	public static final String LOG_NOTICE = "\n" +
			"--------------------------------------------------------------" +
			"\n";

	public static void main(String[] args) {

		try (SparkSession spark = SparkSession.builder()
				.appName(APP_NAME)
				// .master("spark://spark:7077")
				.getOrCreate()) {

			Dataset<Row> diskFailureDf;

			try {

				switch (args[0].toUpperCase(Locale.getDefault())) {
					case "CSV":
						diskFailureDf = spark.read().option("header", true)
								.csv("hdfs://namenode:54310/user/spark/pipeline/preprocessed/data.csv");
						break;
					case "PARQUET":
						diskFailureDf = spark.read().option("header", true)
								.parquet("hdfs://namenode:54310/user/spark/pipeline/preprocessed/data.parquet");
						diskFailureDf = diskFailureDf.withColumn("failure", col("failure.member0"))
								.withColumn("vault_id", col("vault_id.member0"))
								.withColumn("serial_number", col("serial_number.member1"))
								.withColumn("s9_power_on_hours", col("s9_power_on_hours.member0"));
						break;
                    default:
						throw new IllegalArgumentException();
				}

				// LOGGER.info(LOG_NOTICE+"DISK FAILURE SCHEMA"+LOG_NOTICE);
				// diskFailureDf.printSchema();

				/**/
				// LOGGER.info(LOG_NOTICE+"DISK FAILURE ADJUSTED SCHEMA"+LOG_NOTICE);
				// diskFailureDfAdjusted.printSchema();
				LOGGER.info(LOG_NOTICE + "Class App will launc all the query" + LOG_NOTICE);
				LOGGER.info(LOG_NOTICE + "TO RUN QUERY ONE type --class dissanuddinahmed.QueryOne" + LOG_NOTICE);
				LOGGER.info(LOG_NOTICE + "launching query one" + LOG_NOTICE);
				QueryOne.firstQuery(diskFailureDf);
				LOGGER.info(LOG_NOTICE + "TO RUN QUERY TWO type --class dissanuddinahmed.QueryTwo" + LOG_NOTICE);
				LOGGER.info(LOG_NOTICE + "launching query two" + LOG_NOTICE);
				QueryTwo.secondQuery(diskFailureDf);
				LOGGER.info(LOG_NOTICE + "TO RUN QUERY THREE type --class dissanuddinahmed.QueryThree" + LOG_NOTICE);
				LOGGER.info(LOG_NOTICE + "launching query three" + LOG_NOTICE);
				QueryThree.thirdQuery(diskFailureDf);

			} catch (ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
				LOGGER.warning("BAD Args" + e.getMessage());
			}

		} catch (Exception e) {
			Logger.getLogger("[SABD] BATCH error").warning(e.getMessage());
		}
	}

}

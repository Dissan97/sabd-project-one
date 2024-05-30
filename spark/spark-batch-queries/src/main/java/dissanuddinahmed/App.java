package dissanuddinahmed;


import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import java.util.logging.Logger;

public class App {

	private static final String APP_NAME = "[SABD2324] Project 1";
	private static final Logger LOGGER = Logger.getLogger(APP_NAME);
	public static final String LOG_NOTICE = "\n"+
	"--------------------------------------------------------------"+
	"\n";
static void firtsQuery(Dataset<Row> df) {
		df.printSchema();

		Dataset<Row> failuresDf = df.filter(col("failure").equalTo(1));

		Dataset<Row> groupDataVaultId = failuresDf.groupBy("date", "vault_id")
				.agg(count("failure").alias("number_of_failures"));

		Dataset<Row> resDf = groupDataVaultId.filter(col("number_of_failures").between(2, 4))
				.withColumn("date",
						date_format(to_timestamp(col("date"), "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
								"dd-MM-yyyy"))
				.orderBy(col("date"));

		resDf.show();

		resDf.write().mode("overwrite")
				.option("header", "true")
				.option("sep", ",")
				.csv("hdfs://namenode:54310/pipeline/results/firtsQuery");
	}

	static void secondQuery(Dataset<Row> df) {

		Dataset<Row> failuresDf = df.filter(col("failure").equalTo("1"));

		Dataset<Row> perModelFailureDf = failuresDf.groupBy("model")
				.count()
				.orderBy(desc("count"))
				.limit(10);

		perModelFailureDf.show();

		perModelFailureDf.write().mode("overwrite")
				.option("header", "true")
				.option("sep", ",")
				.csv("hdfs://namenode:54310/pipeline/results/secondQuery/TopModels");

		Dataset<Row> perVaultFailure = failuresDf.groupBy("vault_id").count();
		perVaultFailure.show(false);

		Dataset<Row> topPerFailureVault = perVaultFailure.orderBy(desc("count")).limit(10);

		Dataset<Row> topPerVaultWithFailureDf = topPerFailureVault.join(failuresDf, "vault_id");

		Dataset<Row> vaultModelDf = topPerVaultWithFailureDf.groupBy("vault_id", "count")
				.agg(collect_set("model").alias("models"))
				.orderBy(desc("count"))
				.limit(10)
				.withColumn("models", concat_ws(";", col("models")));

		vaultModelDf.show(false);

		vaultModelDf.write().mode("overwrite")
				.option("header", "true")
				.option("sep", ",")
				.csv("hdfs://namenode:54310/pipeline/results/second/topVaultWithModel");

	}

	public static Dataset<Row> calculatePercentiles(Dataset<Row> df) {
		return df.groupBy("failure").agg(
				functions.expr("percentile_approx(s9_power_on_hours, 0.0)").alias("min"),
				functions.expr("percentile_approx(s9_power_on_hours, 0.25)").alias("25th_percentile"),
				functions.expr("percentile_approx(s9_power_on_hours, 0.50)").alias("50th_percentile"),
				functions.expr("percentile_approx(s9_power_on_hours, 0.75)").alias("75th_percentile"),
				functions.expr("percentile_approx(s9_power_on_hours, 1.0)").alias("max"),
				count("s9_power_on_hours").alias("count"));
	}

	public static void ThirdQuery(Dataset<Row> df) {
		// Define the window specification
		WindowSpec windowSpec = Window.partitionBy("serial_number").orderBy(col("date").desc());

		// Add a row number to each record partitioned by serial_number
		Dataset<Row> dfWithRowNum = df.withColumn("row_num", row_number().over(windowSpec));

		// Filter to keep only the latest record of each hard disk
		Dataset<Row> dfLatest = dfWithRowNum.filter(col("row_num").equalTo(1)).drop("row_num");

		// Calculate statistics for disks with failures
		Dataset<Row> failuresStats = calculatePercentiles(dfLatest.filter(col("failure").equalTo(1)));

		// Calculate statistics for disks without failures
		Dataset<Row> noFailuresStats = calculatePercentiles(dfLatest.filter(col("failure").equalTo(0)));

		// Union the results
		Dataset<Row> finalStats = failuresStats.union(noFailuresStats);
		finalStats.show();

		finalStats.write()
				.mode("overwrite")
				.option("header", "true")
				.option("sep", ",")
				.csv("hdfs://namenode:54310/pipeline/results/thirdQuery");
	}

	public static void main(String[] args) {

		try (SparkSession spark = SparkSession.builder()
				.appName(APP_NAME)
				.master("spark://spark:7077")
				.getOrCreate()) {

			Dataset<Row> diskFailureDf = spark.read().option("header", true)
					.parquet("hdfs://namenode:54310/pipeline/preprocessed/data.parquet");

			LOGGER.info(LOG_NOTICE+"DISK FAILURE SCHEMA"+LOG_NOTICE);
			diskFailureDf.printSchema();

			Dataset<Row> diskFailureDfAdjusted = diskFailureDf.withColumn("failure", col("failure.member0"))
					.withColumn("vault_id", col("vault_id.member0"))
					.withColumn("serial_number", col("serial_number.member1"))
					.withColumn("s9_power_on_hours", col("s9_power_on_hours.member0"));
			LOGGER.info(LOG_NOTICE+"DISK FAILURE ADJUSTED SCHEMA"+LOG_NOTICE);
			diskFailureDfAdjusted.printSchema();
			LOGGER.info(LOG_NOTICE+"launching query one"+LOG_NOTICE);
			firtsQuery(diskFailureDfAdjusted);
			LOGGER.info(LOG_NOTICE+"launching query two"+LOG_NOTICE);
			secondQuery(diskFailureDfAdjusted);
			LOGGER.info(LOG_NOTICE+"launching query three"+LOG_NOTICE);
			ThirdQuery(diskFailureDfAdjusted);
			
		} catch (Exception e) {
			Logger.getLogger("[SABD] BATCH error").warning(e.getMessage());
		}
	}

}

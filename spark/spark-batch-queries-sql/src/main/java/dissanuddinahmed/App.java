package dissanuddinahmed;

import java.util.logging.Logger;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class App 
{

  private static final String APP_NAME = "[SABD2324] Project 1 sql";
	private static final Logger LOGGER = Logger.getLogger(APP_NAME);
  	public static final String LOG_NOTICE = "\n"+
	"--------------------------------------------------------------"+
	"\n";

    public static void firtsQuerySql(SparkSession spark) {
    
      Dataset<Row> resDf = spark.sql(
        "SELECT date, vault_id, number_of_failures " +
        "FROM (SELECT date, vault_id, COUNT(failure) as number_of_failures "+
        "FROM diskFailures "+
        "WHERE failure = '1' "+
        "GROUP BY date, vault_id "+
        "HAVING COUNT(failure) BETWEEN 2 AND 4) "+
        "ORDER BY date"
      );
      //resDf.printSchema();
      //res.show();
      resDf.write().mode("overwrite")
				.option("header", "true")
				.option("sep", ",")
				.csv("hdfs://namenode:54310/user/spark/pipeline/results/firtsQuery");
    }


    public static void secondQuerySql(SparkSession spark) {
    
      Dataset<Row> resModelDf = spark.sql(
        "SELECT model, COUNT(failure) as number_of_failures "+
        "FROM diskFailures "+
        "WHERE failure = '1' "+
        "GROUP BY model "+
        "ORDER BY COUNT(failure) DESC "+
        "LIMIT 10"
      );
      //resModelDf.show();
      resModelDf.write().mode("overwrite")
                .option("header", "true")
                .option("sep", ",")
                .csv("hdfs://namenode:54310/user/spark/pipeline/results/secondQuery/TopModels");
      Dataset<Row> resultVaultPerModelDf = spark.sql(
        "WITH perVaultFailure AS ( " +
        "  SELECT vault_id, COUNT(*) AS failure_count " +
        "  FROM diskFailures " +
        "  WHERE failure = 1 " +
        "  GROUP BY vault_id " +
        "), " +
        "topPerFailureVault AS ( " +
        "  SELECT vault_id, failure_count " +
        "  FROM perVaultFailure " +
        "  ORDER BY failure_count DESC " +
        "  LIMIT 10 " +
        "), " +
        "topPerVaultWithFailureDf AS ( " +
        "  SELECT tpf.vault_id, df.model, tpf.failure_count " +
        "  FROM topPerFailureVault tpf " +
        "  JOIN diskFailures df " +
        "  ON tpf.vault_id = df.vault_id " +
        "  WHERE df.failure = 1 " +
        "), " +
        "vaultModelDf AS ( " +
        "  SELECT vault_id, failure_count, collect_set(model) AS models " +
        "  FROM topPerVaultWithFailureDf " +
        "  GROUP BY vault_id, failure_count " +
        "  ORDER BY failure_count DESC " +
        "  LIMIT 10 " +
        ") " +
        "SELECT vault_id, failure_count, concat_ws(';', models) AS models " +
        "FROM vaultModelDf"

    );

    //resultVaultPerModelDf.show(false);
    resultVaultPerModelDf.write().mode("overwrite")
                .option("header", "true")
                .option("sep", ",")
                .csv("hdfs://namenode:54310/user/spark/pipeline/results/secondQuery/topVaultWithModel");

    }

    public static void main( String[] args ){
      
      try (SparkSession spark = SparkSession.builder()
				.appName(APP_NAME)
				.master("spark://spark:7077")
				.getOrCreate()) {

			  Dataset<Row> diskFailureDf = spark.read().option("header", true)
					  .parquet("hdfs://namenode:54310/user/spark/pipeline/preprocessed/data.parquet");

			  LOGGER.info(LOG_NOTICE+"DISK FAILURE SCHEMA"+LOG_NOTICE);
			  diskFailureDf.printSchema();
        diskFailureDf.createOrReplaceTempView("tempView");
        
        Dataset<Row> diskFailureAdjusted = spark.sql(
        "SELECT date_format(to_timestamp("+
        "date, \"yyyy-MM-dd'T'HH:mm:ss.SSSSSS\"), 'dd-MM-yyyy') as date, "+
        "serial_number.member1 as serial_number, "+
        "model, vault_id.member0 as vault_id, "+
        "s9_power_on_hours.member0 as s9_power_on_hours, "+
        "failure.member0 as failure "+
        "FROM tempView"
        );
        diskFailureAdjusted.show();
        diskFailureAdjusted.createOrReplaceTempView("diskFailures");
        firtsQuerySql(spark);
        secondQuerySql(spark);
        spark.stop();
    }
  }
}


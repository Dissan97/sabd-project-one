package dissanuddinahmed;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_set;
import static org.apache.spark.sql.functions.concat_ws;
import static org.apache.spark.sql.functions.desc;

import java.util.Locale;
import java.util.logging.Logger;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class QueryTwo {
    static void secondQuery(Dataset<Row> df) {

        Dataset<Row> failuresDf = df.filter(col("failure").equalTo("1"));

        Dataset<Row> perModelFailureDf = failuresDf.groupBy("model")
                .count()
                .orderBy(desc("count"))
                .withColumnRenamed("count", "failures_count")
                .limit(10);

        //perModelFailureDf.show();
        perModelFailureDf.explain(true);
        perModelFailureDf.write().mode("overwrite")
                .option("header", "true")
                .option("sep", ",")
                .csv("hdfs://namenode:54310/user/spark/pipeline/results/secondQuery/TopModels");

        Dataset<Row> perVaultFailure = failuresDf.groupBy("vault_id").count();
        //perVaultFailure.show(false);

        Dataset<Row> topPerFailureVault = perVaultFailure.orderBy(desc("count")).limit(10);

        Dataset<Row> topPerVaultWithFailureDf = topPerFailureVault.join(failuresDf, "vault_id");

        Dataset<Row> vaultModelDf = topPerVaultWithFailureDf.groupBy("vault_id", "count")
                .agg(collect_set("model").alias("models"))
                .orderBy(desc("count"))
                .limit(10)
                .withColumn("models", concat_ws(";", col("models")))
                .withColumnRenamed("count", "failures_count")
                .withColumnRenamed("models", "list_of_models");

        vaultModelDf.explain(true);
        vaultModelDf.write().mode("overwrite")
                .option("header", "true")
                .option("sep", ",")
                .csv("hdfs://namenode:54310/user/spark/pipeline/results/secondQuery/topVaultWithModel");

    }

    public static void main(String[] args) {
        try {
            try (SparkSession spark = SparkSession.builder()
                    .appName(App.APP_NAME + ".Query-two"+"."+args[0]+"."+args[1])
                    // .master("spark://spark:7077")
                    .getOrCreate()) {

                Dataset<Row> diskFailureDf = null;

                switch (args[1].toUpperCase(Locale.getDefault())) {
                    case "CSV":
                        diskFailureDf = spark.read().option("header", true)
                                .csv("hdfs://namenode:54310/user/spark/pipeline/preprocessed/data.csv");
                        break;
                    case "PARQUET":
                        diskFailureDf = spark.read().option("header", true)
                                .parquet("hdfs://namenode:54310/user/spark/pipeline/preprocessed/data.parquet");
                        diskFailureDf = diskFailureDf.withColumn("failure", col("failure.member0"))
                                .withColumn("vault_id", col("vault_id.member0"))
                                .withColumn("serial_number", col("serial_number.member1"));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                diskFailureDf.select("vault_id", "failure", "model");
                diskFailureDf.explain(true);
                secondQuery(diskFailureDf);
            } catch (Exception e) {
                Logger.getLogger("[SABD] BATCH error").warning(e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
            App.LOGGER.warning("BAD Args" + e.getMessage());
        }

    }
}

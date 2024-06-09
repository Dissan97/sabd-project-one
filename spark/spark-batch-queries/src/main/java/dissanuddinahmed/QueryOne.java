package dissanuddinahmed;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.date_format;
import static org.apache.spark.sql.functions.to_timestamp;
import static org.apache.spark.sql.functions.desc;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class QueryOne {

    static void firstQuery(Dataset<Row> df) {
        //df.printSchema();

        Dataset<Row> failuresDf = df.filter(col("failure").equalTo(1));

        Dataset<Row> groupDataVaultId = failuresDf.groupBy("date", "vault_id")
                .agg(count("failure").alias("failures_count"));

        Dataset<Row> resDf = groupDataVaultId.filter(col("failures_count").between(2, 4))
                .withColumn("date",
                    date_format(to_timestamp(col("date"), "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
                        "dd-MM-yyyy"))
//                .groupBy("date_of_record", "vault_id")
                .withColumnRenamed("date", "date_of_failure")
                .orderBy(desc("failures_count"));

        resDf.show();
        resDf.explain(true);
        resDf.write().mode("overwrite")
             .option("header", "true")
             .option("sep", ",")
             .csv("hdfs://namenode:54310/user/spark/pipeline/results/firtsQuery");

    }

    public static void main(String[] args) {
        try {
            try (SparkSession spark = SparkSession.builder()
                    .appName(App.APP_NAME + ".Query-one"+"."+args[0]+"."+args[1])
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
                                .withColumn("vault_id", col("vault_id.member0"));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                diskFailureDf.select("date", "failure", "vault_id");
                diskFailureDf.explain(true);
                firstQuery(diskFailureDf);
            } catch (Exception e) {
                Logger.getLogger("[SABD] BATCH error").warning(e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
            App.LOGGER.warning("BAD Args" + e.getMessage());
        }

    }

}

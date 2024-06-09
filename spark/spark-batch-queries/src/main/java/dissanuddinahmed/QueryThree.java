package dissanuddinahmed;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.row_number;

import java.util.Locale;
import java.util.logging.Logger;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

public class QueryThree {

    public static Dataset<Row> calculatePercentiles(Dataset<Row> df) {
        return df.groupBy("failure").agg(
                functions.expr("percentile_approx(s9_power_on_hours, 0.0)").alias("min"),
                functions.expr("percentile_approx(s9_power_on_hours, 0.25)").alias("percentile_25"),
                functions.expr("percentile_approx(s9_power_on_hours, 0.50)").alias("percentile_50"),
                functions.expr("percentile_approx(s9_power_on_hours, 0.75)").alias("percentile_75"),
                functions.expr("percentile_approx(s9_power_on_hours, 1.0)").alias("max"),
                count("s9_power_on_hours").alias("count"));
    }

    public static void thirdQuery(Dataset<Row> df) {
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
        //finalStats.show();
        finalStats.explain(true);

        finalStats.write()
                .mode("overwrite")
                .option("header", "true")
                .option("sep", ",")
                .csv("hdfs://namenode:54310/user/spark/pipeline/results/thirdQuery");
    }

    public static void main(String[] args) {
        try {
            try (SparkSession spark = SparkSession.builder()
                    .appName(App.APP_NAME + ".Query-three"+"."+args[0]+"."+args[1])
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
                                .withColumn("serial_number", col("serial_number.member1"))
                                .withColumn("s9_power_on_hours", col("s9_power_on_hours.member0"));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                diskFailureDf.printSchema();
                diskFailureDf.select("s9_power_on_hours", "failure", "serial_number", "date");
                diskFailureDf.explain(true);
                thirdQuery(diskFailureDf);
            } catch (Exception e) {
                Logger.getLogger("[SABD] BATCH error").warning(e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
            App.LOGGER.warning("BAD Args" + e.getMessage());
        }

    }
}

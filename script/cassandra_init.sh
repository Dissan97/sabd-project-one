echo creating keyspace

docker compose exec cassandra-1 cqlsh -e "CREATE KEYSPACE IF NOT EXISTS spark_results 
    WITH REPLICATION = { 
        'class' : 'SimpleStrategy',
        'replication_factor' : 3 
        };"


docker compose exec cassandra-1 cqlsh -e "CREATE TABLE IF NOT EXISTS spark_results.vault_failures (
    vault_id int PRIMARY KEY,
    date_of_failure text,
    failures_count int
    );"
echo creating table query two top_failure_per_vault_models
docker compose exec cassandra-1 cqlsh -e "CREATE TABLE IF NOT EXISTS spark_results.top_failure_per_vault_models (
    vault_id int,
    list_of_models text,
    failures_count int,
    PRIMARY KEY(vault_id, models)
);"

echo creating table query two top_failure_models
docker compose exec cassandra-1 cqlsh -e "CREATE TABLE IF NOT EXISTS spark_results.top_failure_models (
    model text PRIMARY KEY,
    failures_count int
    );"
echo creating table query three
docker compose exec cassandra-1 cqlsh -e "CREATE TABLE IF NOT EXISTS spark_results.percentiles (
    failure int PRIMARY KEY,
    min double,
    percentile_25 double,
    percentile_50 double,
    percentile_75 double,
    max double,
    count int
);"

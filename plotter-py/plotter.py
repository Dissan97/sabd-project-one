from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
import pandas as pd
import matplotlib.pyplot as plt

# Funzione per connettersi a Cassandra e scaricare i dati
def get_data_from_cassandra():
    auth_provider = PlainTextAuthProvider(username='admin', password='admin')
    cluster = Cluster(['127.0.0.1'], port=9042, auth_provider=auth_provider)
    session = cluster.connect()
    session.set_keyspace('spark_results')

    query1 = "SELECT date_of_failure, vault_id, failures_count FROM vault_failures"
    query2_1 = "SELECT model, failures_count FROM top_failure_models"
    query2_2 = "SELECT vault_id, failures_count, list_of_models FROM top_failure_per_vault_models"
    query3 = "SELECT failure, min, percentile_25, percentile_50, percentile_75, max, count FROM percentiles"

    rows1 = session.execute(query1)
    rows2 = session.execute(query2_1)
    rows3 = session.execute(query2_2)
    rows4 = session.execute(query3)

    cluster.shutdown()

    data1 = [row for row in rows1]
    data2 = [row for row in rows2]
    data3 = [row for row in rows3]
    data4 = [row for row in rows4]

    return data1, data2, data3, data4



# Funzione per creare un plot delle date di fallimento
def create_failure_plot(data):
    df = pd.DataFrame(data, columns=['date_of_failure', 'vault_id', 'failures_count'])
    print(df)
    df['date_of_failure'] = pd.to_datetime(df['date_of_failure'], format='%d-%m-%Y')
    df = df.sort_values('date_of_failure')
    df_grouped = df.groupby('date_of_failure')['failures_count'].sum().reset_index()
    plt.figure(figsize=(10, 6))
    plt.plot(df_grouped['date_of_failure'], df_grouped['failures_count'], marker='o')
    plt.title('Fallimenti nel Tempo')
    plt.xlabel('Data')
    plt.ylabel('Numero di Fallimenti')
    plt.grid(True)
    plt.show()
# Funzione per creare un istogramma dei fallimenti per modello
def create_histogram(data):
    df = pd.DataFrame(data, columns=['model', 'failures_count'])
    df.plot(kind='bar', x='model', y='failures_count', legend=False, figsize=(12, 8))
    plt.title('Numero di Fallimenti per Modello')
    plt.xlabel('Modello')
    plt.ylabel('Numero di Fallimenti')
    plt.xticks(rotation=45)
    plt.show()
# Funzione per creare un grafico a barre impilate
def create_stacked_bar_chart(data):
    data_list = []
    for row in data:
        models = row.list_of_models.split(';')
        for model in models:
            data_list.append([row.vault_id, row.failures_count, model])
    df = pd.DataFrame(data_list, columns=['vault_id', 'failures_count', 'list_of_models'])
    df_grouped = df.groupby(['vault_id', 'list_of_models']).size().unstack(fill_value=0)
    df_grouped.plot(kind='bar', stacked=True, figsize=(12, 8))
    plt.title('Numero di Fallimenti per Vault ID e Modello di Disco Rigido')
    plt.xlabel('Vault ID')
    plt.ylabel('Numero di Fallimenti')
    plt.legend(title='Modello di Disco Rigido', bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.tight_layout()
    plt.show()
# Funzione per creare un box plot
def create_box_plot(data):
    df = pd.DataFrame(data, columns=['failure', 'min', 'percentile_25', 'percentile_50', 'percentile_75', 'max', 'count'])
    fig, ax = plt.subplots(figsize=(10, 6))
    box_data = [
        [df['min'][0], df['percentile_25'][0], df['percentile_50'][0], df['percentile_75'][0], df['max'][0]],
        [df['min'][1], df['percentile_25'][1], df['percentile_50'][1], df['percentile_75'][1], df['max'][1]]
    ]
    ax.boxplot(box_data, labels=['Failures (1)', 'No Failures (0)'])
    ax.set_title('Distribuzione dei Tempi di Fallimento')
    ax.set_xlabel('Stato di Fallimento')
    ax.set_ylabel('Tempo')
    plt.show()


# Funzione principale
def main():
    data1, data2, data3, data4 = get_data_from_cassandra()
    create_failure_plot(data1)
    create_histogram(data2)
    create_stacked_bar_chart(data3)
    create_box_plot(data4)


if __name__ == "__main__":
    main()


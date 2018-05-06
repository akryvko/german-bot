# german-bot

## Starting application

Build and publish docker image:
```bash
./gradlew dockerPublish
```

Set env variables:

```bash 
export MESSENGER_VERIFY_TOKEN=...
export MESSENGER_PAGE_ACCESS_TOKEN=...
export MESSENGER_APP_SECRET=...
```

or: 

```bash
source ./setenv.sh
```

Run dynamodb in a local docker container:
```bash
docker run -p8000:8000 -d dwmkerr/dynamodb
```

Run Elasticsearch locally:
```bash
docker run -d --rm -p 9200:9200 --name es_germanbot docker.elastic.co/elasticsearch/elasticsearch:6.2.4
curl -XPUT localhost:9200/_template/quiz -d @src/main/resources/scripts/es_quiz_index.json  -H 'Content-Type: application/json'
```

Run in docker:
```bash
./run.sh
```

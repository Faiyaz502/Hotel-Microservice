

---------Hotel Service ---------- port: 8082


--------User Service -------------  port: 8081


-------Rating Service ---------- port : 8083


--------Eureka Server -----------   port: 8761


Individula DB for all_



----Project DOcket-----

incmd consumer
-docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic user-created --from-beginning --property print.key=true --property print.value=true


in Docker RUN 
Faiyaz@FaiyazFahim MINGW64 /e/Microservice/UserService/src/main/resources (main)
 docker-compose up -d


 Elastic search json - curl -X PUT "http://localhost:9200/hotels" -H "Content-Type: application/json" -d "{\"settings\":{\"analysis\":{\"analyzer\":{\"autocomplete_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"edge_ngram_filter\"]}},\"filter\":{\"edge_ngram_filter\":{\"type\":\"edge_ngram\",\"min_gram\":2,\"max_gram\":10}}}},\"mappings\":{\"properties\":{\"nameSuggest\":{\"type\":\"text\",\"analyzer\":\"autocomplete_analyzer\",\"search_analyzer\":\"standard\"}}}}}"



------___DB REPLICA--------------
# Primary (write node) - port 5432
docker run -d --name pg-primary -e POSTGRES_PASSWORD=root -e POSTGRES_DB=Microservice-hotel -p 5432:5432 -v C:\Postgres\pg-primary:/var/lib/postgresql/data postgres:15

# Replica1 (read node) - port 5433
docker run -d --name pg-replica1 -e POSTGRES_PASSWORD=root -e POSTGRES_DB=Microservice-hotel-r1 -p 5433:5432 -v C:\Postgres\pg-replica1:/var/lib/postgresql/data postgres:15

# Replica2 (read node) - port 5434
docker run -d --name pg-replica2 -e POSTGRES_PASSWORD=root -e POSTGRES_DB=Microservice-hotel-r2 -p 5434:5432 -v C:\Postgres\pg-replica2:/var/lib/postgresql/data postgres:15

# Replica3 (read node) - port 5435
docker run -d --name pg-replica3 -e POSTGRES_PASSWORD=root -e POSTGRES_DB=Microservice-hotel-r3 -p 5435:5432 -v C:\Postgres\pg-replica3:/var/lib/postgresql/data postgres:15


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




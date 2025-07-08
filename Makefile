build:
	./gradlew build -x test
docker:
	docker-compose up -d
down:
	docker-compose down
# Kafka
kafka-up:
	docker-compose -f infra/kafka/docker-compose.yml up -d

kafka-down:
	docker-compose -f infra/kafka/docker-compose.yml down

kafka-logs:
	docker-compose -f infra/kafka/docker-compose.yml logs -f

# Elasticsearch
es-up:
	docker-compose -f infra/elasticsearch/docker-compose.yml up -d

es-down:
	docker-compose -f infra/elasticsearch/docker-compose.yml down

es-logs:
	docker-compose -f infra/elasticsearch/docker-compose.yml logs -f

#전체 컨테이너 up, down
up-all: docker kafka-up es-up

down-all: down es-down kafka-down

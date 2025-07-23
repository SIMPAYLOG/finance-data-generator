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

# ELK
elk-up:
	docker-compose -f infra/elk/docker-compose.yml up -d

elk-down:
	docker-compose -f infra/elk/docker-compose.yml down

elk-logs:
	docker-compose -f infra/elk/docker-compose.yml logs -f

#전체 컨테이너 up, down
up-all: docker kafka-up elk-up

down-all: down elk-down kafka-down

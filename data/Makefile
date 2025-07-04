.PHONY: up down kafka-up kafka-down es-up es-down kafka-logs es-logs

# Kafka
kafka-up:
	docker-compose -f kafka/docker-compose.yml up -d

kafka-down:
	docker-compose -f kafka/docker-compose.yml down

kafka-logs:
	docker-compose -f kafka/docker-compose.yml logs -f

# Elasticsearch
es-up:
	docker-compose -f elasticsearch/docker-compose.yml up -d

es-down:
	docker-compose -f elasticsearch/docker-compose.yml down

es-logs:
	docker-compose -f elasticsearch/docker-compose.yml logs -f

#kafka 및 es 전체 up, down
up: kafka-up es-up

down: es-down kafka-down

# 공통 네트워크 생성 (최초 1회만)
network-create:
	docker network create kafka-net

network-rm:
	docker network rm kafka-net
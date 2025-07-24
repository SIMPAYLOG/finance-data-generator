# Docker 프로젝트 이름. 현재 폴더(Finance-data-generator)를 프로젝트 명으로 지정
PROJECT_NAME := $(notdir $(CURDIR))

build:
	./gradlew build -x test

docker-up:
	docker-compose -p $(PROJECT_NAME) up -d

docker-down:
	docker-compose -p $(PROJECT_NAME) down

docker-logs:
	docker-compose -p $(PROJECT_NAME) logs -f

# Kafka
kafka-up:
	docker-compose -p $(PROJECT_NAME) -f infra/kafka/docker-compose.yml up -d

kafka-down:
	docker-compose -p $(PROJECT_NAME) -f infra/kafka/docker-compose.yml down

kafka-logs:
	docker-compose -p $(PROJECT_NAME) -f infra/kafka/docker-compose.yml logs -f

# ELK
elk-up:
	docker-compose -p $(PROJECT_NAME) -f infra/elk/docker-compose.yml up -d

elk-down:
	docker-compose -p $(PROJECT_NAME) -f infra/elk/docker-compose.yml down

elk-logs:
	docker-compose -p $(PROJECT_NAME) -f infra/elk/docker-compose.yml logs -f

# 전체 컨테이너 up, down
up-all: docker-up kafka-up elk-up

down-all: elk-down kafka-down docker-down

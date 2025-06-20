build:
	./gradlew build -x test
docker:
	docker-compose up -d
down:
	docker-compose down

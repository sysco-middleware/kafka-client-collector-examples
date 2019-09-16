all : clean generate-classes

build: clean build-jar docker-build
clean:
	mvn clean
build-jar:
	mvn package
docker-build:
	docker-compose build
docker-up-kafka:
	docker-compose up -d zookeeper kafka1 kafka2 kafka3
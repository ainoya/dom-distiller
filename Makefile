DOCKER_IMAGE = "dom-distiller"

docker-build:
	docker build -t $(DOCKER_IMAGE) .

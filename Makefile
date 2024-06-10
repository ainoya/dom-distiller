DOCKER_IMAGE = "dom-distiller"

docker-build:
	docker build --cpuset-cpus 0 --platform linux/amd64 -t $(DOCKER_IMAGE) .

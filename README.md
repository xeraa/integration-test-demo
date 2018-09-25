# Demo Project for Integration Testing with Elasticsearch

Heavily inspired by [https://github.com/dadoonet/elasticsearch-integration-tests](https://github.com/dadoonet/elasticsearch-integration-tests).

Prerequisites:

* Docker
* JDK 8+
* Maven 3.5+



## Actual Datastore

The first step is to run Elasticsearch locally. We are using Docker compose for this from within the folder
*0_actual-datastore/*:

```sh
docker-compose up
```

Then you can run the actual test from your IDE or the shell with:

```sh
mvn test
```

Once done, stop and remove your container with:

```sh
docker-compose down -v
```

Note that you can override the Elasticsearch configuration with the environment variables `tests.elasticsearch.host`,
`tests.elasticsearch.port`, and `tests.elasticsearch.scheme`.



## Embedded

Here we have switched to the embedded version that downloads the right binary in the background and runs it for you.
Try to run tests from your IDE, but your shell works as well within the folder *1_embedded/*:

```sh
mvn test
```

Note the resource filtering, which we can use to access the Elasticsearch version from the POM file.



## Build Tool

Now we can start the Docker container from Maven so you can run the following from the folder *2_build-tool/*:

```sh
mvn docker:start test docker:stop
```

But if you run the tests from your IDE they will fail.

Note that everything is configured through properties in the POM file and we are using resource filtering again to
connect to the right endpoint.



## Testcontainers General

Next we add Testcontainers — first through a generic Docker Compose setup.
Try to run tests from your IDE, but the shell works as well from the folder *3_testcontainers-general/*:

```sh
mvn test
```



## Testcontainers Custom

Building on the previous example this approach uses a custom wrapper in Java for Elasticsearch in Testcontainers.
Try to run tests from your IDE, but the shell works as well from the folder *4_testcontainers-custom/*:

```sh
mvn test
```

Note that the port is randomly picked here and we are simply fetching all the configuration values from the container.



## Docker in Docker

Finally, we can demo how to run the previous example from within Docker; running our application in the `maven:3`
container. This will only work on the shell and needs to be run in the base folder:

```sh
docker run -it --rm -v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock maven:3 mvn --projects parent,4_testcontainers-custom test
```


# Upgrade

Change the version in *pom.xml*, *0_actual-datastore/docker-compose.yml*, and *3_testcontainers-general/src/test/resources/docker-compose.yml*.


# Todo

* Randomize port & JUnit5 — depends on Testcontainer support

# Demo Project for Integration Testing with Elasticsearch

[Slides and video](https://xeraa.net/talks/integration-tests-with-containers/) of the talk based on this demo.

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

Also this will currently fail for 7.x because the dependency has not been updated for it yet. Use 6.8 instead, but show
the failure to explain why this is an issue.



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



## Docker Wormhole

Finally, we can demo how to run the previous example from within Docker; running our application in the `maven:3`
container. This will only work on the shell and needs to be run in the base folder:

```sh
docker run -it --rm -v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock -v ~/.m2:/var/maven/.m2 maven:3 mvn --projects parent,4_testcontainers-custom test
```


# Upgrade

Change the version in *pom.xml* and *0_actual-datastore/docker-compose.yml*.


# Credits

Inspired by [https://github.com/dadoonet/elasticsearch-integration-tests](https://github.com/dadoonet/elasticsearch-integration-tests).


# Todo

* https://github.com/alexcojocaru/elasticsearch-maven-plugin
* Move tests to integration tests so that the docker:start and docker:stop run automatically
* https://vanwilgenburg.wordpress.com/2019/01/22/embedded-elasticsearch-junit5-spring-boot/

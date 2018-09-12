# Demo Project for Integration Testing with Elasticsearch

Heavily inspired by [https://github.com/dadoonet/elasticsearch-integration-tests](https://github.com/dadoonet/elasticsearch-integration-tests).



## Actual Datastore

The first step is to install Elasticsearch locally.
If you have Docker installed (which is expected for the next steps anyway), you can run Elasticsearch from this test's folder with:

```sh
docker-compose up
```

Then you can run the actual test from your IDE or the shell with:

```sh
mvn test
```

Once done, remove your container with:

```sh
docker-compose down -v
```



## Embedded

Here we have switched to the embedded version that downloads the right binary in the background and runs it for you.
Try to run tests from your IDE, but your shell works as well:

```sh
mvn test
```



## Build Tool

Now we can start the Docker container from Maven so you can run:

```sh
mvn docker:start test docker:stop
```

But if you run the tests from your IDE they will fail.



## Testcontainers General

Finally, we add Testcontainers — first through a generic Docker Compose setup.
Try to run tests from your IDE, but the shell works as well:

```sh
mvn test
```



## Testcontainers Custom

This approach uses a custom wrapper in Java for Elasticsearch in Testcontainers.
Try to run tests from your IDE, but the shell works as well:

```sh
mvn test
```



## Docker in Docker

Finally, we can demo how to run the previous example from within Docker.
This will only work on the shell again:

```sh
docker run -it --rm -v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock maven:3 mvn --projects :4_testcontainers-custom test
```


# Upgrade

Change the version in *pom.xml*, *0_actual-datastore/docker-compose.yml*, and *3_testcontainers-general/src/test/resources/docker-compose.yml*.


# Todo

* Randomize port & JUnit5 — depends on Testcontainer support

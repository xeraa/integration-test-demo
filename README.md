# Demo Project for Integration Testing with Elasticsearch

Heavily inspired by [https://github.com/dadoonet/elasticsearch-integration-tests](https://github.com/dadoonet/elasticsearch-integration-tests).



## Actual Datastore

The first step is to install Elasticsearch locally.
If you have Docker installed (which is expected for the next steps anyway), you can run Elasticsearch from this test's folder with:

```sh
docker-compose up
```

Then you can run:

```sh
mvn test
```

Or you can run the test from your IDE.

Once done, remove your container with:

```sh
docker-compose down -v
```



## Embedded

Here we have switched to the embedded version that downloads the right binary in the background and runs it automatically.

Try to run tests from your IDE, but Maven works as well:

```sh
mvn test
```



## Build Tool

Now we can start the Docker container from Maven so you can run:

```sh
mvn docker:start test docker:stop
```

But if you run the tests from your IDE they will still fail.



## Testcontainer

Finally, we add Testcontainers to launch our tests from the IDE.

Try to run tests from your IDE, but Maven works as well:

```sh
mvn test
```



# Upgrade

Change the version in *pom.xml* and *0_actual-datastore/docker-compose.yml*.


# Todo


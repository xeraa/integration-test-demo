# Demo project for Integration Testing with Elasticsearch

Heavily inspired by [https://github.com/dadoonet/elasticsearch-integration-tests](https://github.com/dadoonet/elasticsearch-integration-tests).


## Actual Datastore

So the first step is to install Elasticsearch locally.
If you have Docker installed (which is expected for the next steps anyway), you can run from the project root:

```sh
docker-compose up
```

Then you can run:

```sh
mvn test
```

Or run tests from your IDE.

Once done, remove your container with:

```sh
docker-compose down -v
```



## Build Tool

We now starts Docker container from Maven so you can simply run:

```sh
mvn docker:start test docker:stop
```

But if you run the tests from your IDE, tests will still fail.



## Testcontainer

We add https://github.com/dadoonet/testcontainers-java-module-elasticsearch[testcontainers-java-module-elasticsearch] to
launch our tests from the IDE. This project helps to launch an Elasticsearch Docker image from Java.

Try to run tests from your IDE now.

But when we run:

```
mvn test
```

It actually starts Docker twice now:



## Todo


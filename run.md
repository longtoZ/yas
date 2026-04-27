test:

- cài thư viện:
```sh
mvn -N install "-Drevision=1.0-SNAPSHOT"
mvn -am -pl order test "-Drevision=1.0-SNAPSHOT"
```

- unit test:
```sh
mvn -am -pl payment test "-Drevision=1.0-SNAPSHOT"
mvn -am -pl payment-paypal test "-Drevision=1.0-SNAPSHOT"
mvn -am -pl promotion test "-Drevision=1.0-SNAPSHOT"
mvn -am -pl search test "-Drevision=1.0-SNAPSHOT"
mvn -am -pl webhook test "-Drevision=1.0-SNAPSHOT"
```

- integration test:
```sh 
mvn -am -pl payment verify "-Drevision=1.0-SNAPSHOT"
mvn -am -pl payment-paypal verify "-Drevision=1.0-SNAPSHOT"
mvn -am -pl promotion verify "-Drevision=1.0-SNAPSHOT"
mvn -am -pl search verify "-Drevision=1.0-SNAPSHOT"
mvn -am -pl webhook verify "-Drevision=1.0-SNAPSHOT"
```

```sh
mvn -am -pl order verify "-Drevision=1.0-SNAPSHOT" -DskipITs=true
```
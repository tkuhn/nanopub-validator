Validator for Nanopublications
==============================

This is a validator interface for nanopublications.


Run with Docker Compose
-----------------------

    $ docker-compose up


Compilation and Execution with Maven
------------------------------------

Compile and package with Maven:

    $ mvn clean package

Running the program using Maven's Jetty plugin:

    $ mvn jetty:run

Then you should be able to locally access the web interface from your browser:

    http://0.0.0.0:8080/

Alternatively, you can give the file `target/nanopub-validator.war` to a web
application server such as Apache Tomcat.


Build Docker Container
----------------------

    $ mvn clean package
    $ docker build -t nanopub/validator .


License
-------

This validator for nanopublications is free software under the MIT License. See
LICENSE.txt.

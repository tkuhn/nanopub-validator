Validator for Nanopublications
==============================

This is a validator interface for nanopublications.

It can be found at: http://nanopub.inn.ac


Dependencies
------------

Maven has to be installed.

Installation of nanopub-java:

    $ git clone git@github.com:Nanopublication/nanopub-java.git
    $ cd nanopub-java
    $ mvn install

Installation of trustyuri-java:

    $ git clone git@github.com:trustyuri/trustyuri-java.git
    $ cd trustyuri-java
    $ mvn install


Compilation and Execution
-------------------------

Compile and package with Maven:

    $ mvn clean package

Running the program using Maven's Jetty plugin:

    $ mvn jetty:run

Then you should be able to locally access the web interface from your browser:

    http://0.0.0.0:8080/nanopub-validator/

Alternatively, you can give the file `target/nanopub-validator.war` to a web
application server such as Apache Tomcat.


License
-------

This validator for nanopublications is free software under the MIT License. See
LICENSE.txt.

# Pull base image
FROM tomcat:9.0.56-jre17-temurin

# Remove default webapps:
RUN rm -fr /usr/local/tomcat/webapps/*

COPY target/nanopub-validator /usr/local/tomcat/nanopub-validator/target/nanopub-validator
RUN ln -s /usr/local/tomcat/nanopub-validator/target/nanopub-validator /usr/local/tomcat/webapps/ROOT

# Port:
EXPOSE 8080

CMD ["catalina.sh", "run"]
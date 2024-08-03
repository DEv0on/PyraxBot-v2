FROM registry.access.redhat.com/ubi8/openjdk-21:1.19

ENV LANGUAGE='en_US:en'


# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 target/${project.artifactId}-${project.version}.jar /deployments/

USER 185
ENV JAVA_APP_JAR="/deployments/${project.artifactId}-${project.version}.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
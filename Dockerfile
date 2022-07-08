FROM jboss/business-central-workbench

WORKDIR  /opt/jboss/wildfly/bin
USER root

RUN update-alternatives --set "java_sdk_openjdk" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64" && \
    update-alternatives --set "java" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64/bin/java" && \
    update-alternatives --set "javac" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64/bin/javac"

RUN yum -y install epel-release && \
    yum -y update && \
    yum -y install supervisor

ADD /bc-git-integration-push/target/git-push-2.2.0-SNAPSHOT.jar  /opt/jboss/wildfly/bin/git-push-2.2.0-SNAPSHOT.jar
ADD /bc-git-integration-webhook/target/bc-git-integration-webhook-2.2.0-SNAPSHOT.jar  /opt/jboss/wildfly/bin/bc-git-integration-webhook-2.2.0-SNAPSHOT.jar
ADD supervisor.conf /opt/jboss/wildfly/bin

USER jboss

EXPOSE 8080
EXPOSE 8001
EXPOSE 9090

CMD ["supervisord", "-c", "/opt/jboss/wildfly/bin/supervisor.conf"]
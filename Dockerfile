FROM jboss/business-central-workbench

WORKDIR  /opt/jboss/wildfly/bin
USER root

RUN update-alternatives --set "java_sdk_openjdk" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64" && \
    update-alternatives --set "java" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64/bin/java" && \
    update-alternatives --set "javac" "/usr/lib/jvm/java-11-openjdk-11.0.8.10-0.el7_8.x86_64/bin/javac"

RUN yum -y install epel-release && \
    yum -y update && \
    yum -y install supervisor && \
    yum -y install gettext

COPY .gitremote /opt/jboss/gitremote
COPY entrypoint.sh /opt/jboss/wildfly/bin/entrypoint.sh
RUN chmod -R 777 /opt/jboss
RUN chmod -R 777 /root

ADD /bc-git-integration-push/target/git-push-2.2.0-SNAPSHOT.jar  /opt/jboss/wildfly/bin/git-push-2.2.0-SNAPSHOT.jar
ADD /bc-git-integration-webhook/target/bc-git-integration-webhook-2.2.0-SNAPSHOT.jar  /opt/jboss/wildfly/bin/bc-git-integration-webhook-2.2.0-SNAPSHOT.jar
ADD supervisor.conf /opt/jboss/wildfly/bin/supervisor.conf

ENV GITLAB_GROUP ""
ENV GIT_TOKEN ""
ENV BC_LOGIN ""
ENV REMOTE_GIT_URL ""
ENV GIT_LOGIN ""
ENV BC_PASSWD ""
ENV GIT_PROVIDER ""
ENV GIT_PASSWD ""

EXPOSE 8080
EXPOSE 8001
EXPOSE 9090

CMD ["./entrypoint.sh"]
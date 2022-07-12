#!/bin/bash
/usr/bin/envsubst </opt/jboss/gitremote >/opt/jboss/.gitremote
/usr/bin/envsubst </opt/jboss/gitremote >/root/.gitremote
/opt/jboss/wildfly/bin/add-user.sh -a -u user -p password -g user,admin

supervisord -c /opt/jboss/wildfly/bin/supervisor.conf
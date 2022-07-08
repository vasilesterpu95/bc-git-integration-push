#!/bin/bash
/usr/bin/envsubst </opt/jboss/gitremote >/opt/jboss/.gitremote
/usr/bin/envsubst </opt/jboss/gitremote >/root/.gitremote

supervisord -c /opt/jboss/wildfly/bin/supervisor.conf
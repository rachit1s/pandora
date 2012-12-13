#! /bin/sh
JAVA_HOME=${JDKPath}
TBITS_PORT=${build.transbit.tbits.port}
TBITS_INSTALL_PATH=${INSTALL_PATH}
TBITS_HOME=$TBITS_INSTALL_PATH/build
CATALINA_BASE=$TBITS_INSTALL_PATH/tomcat
CATALINA_TMPDIR=$TBITS_INSTALL_PATH/tmp
CATALINA_LOGDIR=$TBITS_INSTALL_PATH/tomcat/logs
#JAVA_OPTS="-Xmx512m -agentpath:/Users/sandeepgiri/myapps/YourKit_Java_Profiler_8.0.17.app/bin/mac/libyjpagent.jnilib=disablestacktelemetry,disableexceptiontelemetry,delay=10000"
JAVA_OPTS="-Xmx1024m -XX:MaxPermSize=256M"
CATALINA_HOME="${CATALINA_BASE}"
CATALINA_OPTS="-Dapp.name=tbits -Dtbits.home=$TBITS_HOME -Dapp.serviceName=WebService -Dport.http.nonssl=$TBITS_PORT"
export JAVA_HOME JAVA_OPTS CATALINA_BASE CATALINA_OPTS
#export CLASSPATH=$CLASSPATH:$TBITS_HOME/plugins


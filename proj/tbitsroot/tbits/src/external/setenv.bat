set JAVA_HOME=${JDKPath}
set TBITS_PORT=${build.transbit.tbits.port}
set TBITS_INSTALL_PATH=%CD%\..\..
set TBITS_HOME=%TBITS_INSTALL_PATH%/build
set CATALINA_BASE=%TBITS_INSTALL_PATH%/tomcat
set CATALINA_TMPDIR=%TBITS_INSTALL_PATH%/tmp
set CATALINA_LOGDIR=%TBITS_INSTALL_PATH%/tomcat/logs
set JAVA_OPTS=-Xmx1024m -XX:MaxPermSize=256M
set CATALINA_HOME=%CATALINA_BASE%
set CATALINA_OPTS=-Dapp.name=tbits -Dtbits.home="%TBITS_HOME%" -Dapp.serviceName=WebService -Dport.http.nonssl=%TBITS_PORT%

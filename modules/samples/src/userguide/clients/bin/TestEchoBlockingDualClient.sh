export AXIS2_HOME=../../../../../..
AXIS2_CLASSPATH=$AXIS2_HOME/lib/axis2-0.91.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/axis-wsdl4j-1.2.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/commons-logging-1.0.3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/log4j-1.2.8.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/stax-1.1.2-dev.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/stax-api-1.0.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/geronimo-spec-activation-1.0.2-rc4.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/commons-codec-1.3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/commons-httpclient-3.0-rc3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/geronimo-spec-javamail-1.3.1-rc5.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/groovy-all-1.0-jsr-01.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/jaxen-1.1-beta-7.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/xbean-2.0.0-beta1.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/XmlSchema-0.9.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/xmlunit-1.0.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:../../../../sample.jar
export AXIS2_CLASSPATH
java -classpath $AXIS2_CLASSPATH userguide.clients.EchoBlockingDualClient

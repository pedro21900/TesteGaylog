# TesteGaylog


Para instalar GrayLog via Docker copie os seguintes comandos :

```
sudo docker run --name mongo -d mongo:4.2
sudo docker run --name elasticsearch \
    -e "http.host=0.0.0.0" \
    -e "discovery.type=single-node" \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -d docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2
sudo docker run --name graylog --link mongo --link elasticsearch \
    -p 9000:9000 -p 12201:12201 -p 12201:12201/udp -p 514:514 -p 1514:1514 -p 1514:1514/udp -p 5555:5555 -p 5555:5555/udp \
    -e GRAYLOG_HTTP_PUBLISH_URI="http://10.13.16.207:9000/" \
    -d graylog/graylog:4.0
    
```
    
- Após a instalação ter sido feita , você precisará enviar mensagens da sua aplicação para o Graylog 
    
  - existem 3 formas de envio das quais somente duas seram demontradas :
    
### 1. Logback
    
    crie um arquivo no  /src/mainresources/logback.xml
    
```
<configuration>
    <conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter" />
    <conversionRule conversionWord="wex" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter" />
    <conversionRule conversionWord="wEx" converterClass="org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter" />
    <property name="CONSOLE_LOG_PATTERN" value="${CONSOLE_LOG_PATTERN:-%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"/>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <appender name="GELF" class="de.siegmar.logbackgelf.GelfUdpAppender">
        <graylogHost><IP_SERVER_GRAYLOG></graylogHost>
        <graylogPort><PORT_GRAYLOG></graylogPort>
    </appender>

    <!-- Console output log level -->
    <root level="info">
        <appender-ref ref="GELF" />
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```
### 2. Log4j2
    
    crie um arquivo no  /src/mainresources/log4j2-spring.xml
    
```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="OFF" packages="org.graylog2.log4j2">
    <Properties>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss:SSS} - %-5level  - %pid - %t - %c{1.}:%L - %m%n</Property>
    </Properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT" follow="true">
            <ThresholdFilter level="trace" onMatch="ACCEPT" onMismatch="DENY" />
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
        <GELF name="gelfAppender" server="<IP_SERVER_GRAYLOG>" port="<PORT_GRAYLOG>" hostName="appserver01.example.com">
            <PatternLayout pattern="%logger{36} - %msg%n"/>
            <Filters>
                <Filter type="MarkerFilter" marker="FLOW" onMatch="DENY" onMismatch="NEUTRAL"/>
                <Filter type="MarkerFilter" marker="EXCEPTION" onMatch="DENY" onMismatch="ACCEPT"/>
            </Filters>
            &lt;!&ndash; Additional fields &ndash;&gt;
            <KeyValuePair key="foo" value="bar"/>
            <KeyValuePair key="jvm" value="${java:vm}"/>
        </GELF>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="gelfAppender"/>
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```    

##### Somente isso é necessário para enviar mensagens na porta escolhida para o Graylog  
- -e GRAYLOG_HTTP_PUBLISH_URI="http://10.13.16.207:9000/" \ -> responsavél por publicar nesse ip e nesta porta a api rest e web do graylog 
- -p 9000:9000 -p 12201:12201 -p 12201:12201/udp -p 514:514 -p 1514:1514 -p 1514:1514/udp -p 5555:5555 -p 5555:5555/udp \ portas udp e tcp que seram usadas 

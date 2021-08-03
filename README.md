## TesteGaylog

Usando aplicação-empy Spring-boot para implementação de envio de mensagens ao centralizador de Logs e alterção na formatação dos logs.  

#### Passo 1 : Instalação GrayLog

##### Para instalar o servidor GrayLog via Docker copie os seguintes comandos :

```
sudo docker run --name mongo --rm  -d mongo:4.2
sudo docker run --name elasticsearch  \
    -e "http.host=0.0.0.0" \
    -e "discovery.type=single-node" \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -d docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2
sudo docker run --name graylog --link mongo --link elasticsearch --rm \
    -p 9000:9000 -p 12201:12201 -p 12201:12201/udp -p 514:514 -p 1514:1514 -p 1514:1514/udp -p 5555:5555 -p 5555:5555/udp \
    -e GRAYLOG_HTTP_PUBLISH_URI="http://10.13.16.207:9000/" \
    -e TZ=America/Belem \
    -e GRAYLOG_TIMEZONE=America/Belem \
    -d graylog/graylog:4.1
    
```



Somente isso é necessário para enviar mensagens na descrita abaixo : 
1. -e GRAYLOG_HTTP_PUBLISH_URI="http://10.13.16.207:9000/" \ -> responsavél por publicar nesse ip e nesta porta a api rest e web do graylog 
2. -p 9000:9000 -p 12201:12201 -p 12201:12201/udp -p 514:514 -p 1514:1514 -p 1514:1514/udp -p 5555:5555 -p 5555:5555/udp \ portas udp e tcp que seram usadas o Graylog usa por padrão a porta 12201 oara se comunicar com a aplicação.  
    
Após tudo instalado , a aplicação tem algumas formas se comunicar com o graylog , uma delas é o GELF ,através de:

- Log4j2 que é a nova versão do Log4j (Oficial).
- Lockback (Terceiros).

Com isso temos apenas que escolher qual dependência usar:

```xml
  <!--para Log4j2 (Oficial) -->
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter</artifactId>
             <exclusions>
                 <exclusion>
                     <artifactId>spring-boot-starter-logging</artifactId>
                     <groupId>org.springframework.boot</groupId>
                 </exclusion>
             </exclusions>
         </dependency>
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-log4j2</artifactId>
         </dependency>
         <dependency>
             <groupId>org.graylog2.log4j2</groupId>
             <artifactId>log4j2-gelf</artifactId>
             <version>1.3.1</version>
         </dependency>

        <!--para Logback(Terceiro) -->
        <dependency>
            <groupId>org.graylog2</groupId>
            <artifactId>gelfj</artifactId>
            <version>1.1.16</version>
        </dependency>
        <dependency>
            <groupId>de.siegmar</groupId>
            <artifactId>logback-gelf</artifactId>
            <version>2.0.0</version>
        </dependency
```



### 1. Logback ( Terceiros )
    
    crie um arquivo no  /src/main/resources/logback.xml
    
```xml
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
### 2. Log4j2 (Oficial)
    
    crie um arquivo no  /src/main/resources/log4j2-spring.xml
    
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="OFF" packages="org.graylog2.log4j2">
    <Properties>
        <Property name="LOG_PATTERN">[%d{yyyy-MM-dd HH:mm:ss:SSS}{GMT-3}] - [%highlight{%-5level}{ERROR=Bright RED, WARN=Bright Yellow, INFO=Bright Green, DEBUG=Bright Cyan, TRACE=Bright White} - [%pid] - %t] - %c{1.}:%L - %m%n</Property>
    </Properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT" follow="true">
            <ThresholdFilter level="trace" onMatch="ACCEPT" onMismatch="DENY" />
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
        <GELF name="gelfAppender" server="10.13.16.207" port="12201" hostName="CORVO" protocol="UDP">
            <PatternLayout pattern="%logger{36} - %msg%n"/>
            <Filters>
                <Filter type="MarkerFilter" marker="FLOW" onMatch="DENY" onMismatch="NEUTRAL"/>
                <Filter type="MarkerFilter" marker="EXCEPTION" onMatch="DENY" onMismatch="ACCEPT"/>
            </Filters>
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

Escolhida a dependência e o arquivo implementado,  digite **mvn clean package** e excute a aplicação , feito isto será necessário a configuração no GayLog

<img src=https://github.com/pedro21900/TesteGaylog/tree/main/src/main/resources/Peek%2003-08-2021%2011-54.gif>



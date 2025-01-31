<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="logName" source="logging.file.name" defaultValue="log.log" />
    <!--定义日志文件的存储地址-->
    <property name="LOG_HOME" value="${logName}" />
    <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg：日志消息，%n是换行符-->
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n" />

    <!-- 控制台输出 -->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    <!-- 输出到日志文件 -->
    <appender name="FILE"  class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!--日志文件输出的文件名-->
            <FileNamePattern>${LOG_HOME}.%d{yyyy-MM-dd}.%i</FileNamePattern>
            <MaxHistory>30</MaxHistory>
            <MaxFileSize>50MB</MaxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!--配置logstash地址-->
    <appender name="logstash" class="net.logstash.logback.appender.LogstashAccessTcpSocketAppender">
        <destination>192.168.64.102:5044</destination>
        <encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <!-- 自定义logger -->
    <logger name="com.dispart" level="debug" additivity="false">
        <appender-ref ref="console" />
    </logger>
    <!--sql语句执行输出-->
    <logger name="org.apache.ibatis" level="debug" additivity="false">
        <appender-ref ref="console" />
    </logger>

    <root level="info" additivity="false">
        <appender-ref ref="console" />
<!--        将日志输出到logstash-->
        <appender-ref ref="logstash"/>
    </root>
</configuration>
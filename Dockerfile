# 指定基础镜像
FROM amazoncorretto:21.0.3

# 拷贝jdk和java项目的包
COPY ./target/illness-1.0-SNAPSHOT.jar /illness/illness.jar

# 暴露端口
EXPOSE 8080
# 入口，java项目的启动命令
ENTRYPOINT java -jar -Xms4g -Xmx4g /illness/illness.jar --spring.profiles.active=pro

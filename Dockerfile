FROM openjdk:17-jdk-slim
WORKDIR /app
# 将打包好的jar包拷贝到镜像中
COPY LiuMing-services/target/LiuMing-services-0.0.1-SNAPSHOT.jar app.jar
# 暴露端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

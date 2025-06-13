# 使用官方的 OpenJDK 21运行时作为父镜像
FROM openjdk:21-jdk-slim

# 设置容器内的工作目录
WORKDIR /app

# 将可执行的 JAR 文件复制到容器中
# 请将 'your-application.jar' 替换为你的 JAR 文件的实际名称
COPY target/yuncang-0.0.1-SNAPSHOT.jar app.jar

# 使容器的8080端口可以被外部访问
EXPOSE 8080

# 运行 JAR 文件
ENTRYPOINT ["java", "-jar", "app.jar"]
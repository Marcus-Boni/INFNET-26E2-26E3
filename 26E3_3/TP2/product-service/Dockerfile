# Estágio 1: Compilação e empacotamento da aplicação
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia as definições de dependência e código-fonte
COPY pom.xml .
COPY src ./src

# Executa o build da aplicação ignorando testes unitários durante a geração da imagem
RUN mvn clean package -DskipTests

# Estágio 2: Imagem enxuta de runtime baseada em Alpine Linux
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o artefato compilado do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta padrão do serviço
EXPOSE 8081
ENV SERVER_PORT=8081

# Ponto de entrada para execução da JVM
ENTRYPOINT ["java", "-jar", "app.jar"]

# Estágio 1: Build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Baixa as dependências do Maven (aproveita cache de camadas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia os arquivos-fonte e compila gerando o pacote WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Imagem de execução com Apache Tomcat 10.1 (Jakarta EE 10 / Servlet 6.0)
FROM tomcat:10.1-jdk17-temurin

# Remove as aplicações padrão do Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia o WAR gerado para a raiz do Tomcat (ROOT.war)
COPY --from=builder /app/target/Crud_em_grupo.war /usr/local/tomcat/webapps/ROOT.war

# Cria a pasta para o banco H2 e inclui os dados iniciais do projeto
RUN mkdir -p /app/data
COPY --from=builder /app/src/data/ /app/data/

# Define a variável de ambiente apontando para o arquivo do banco H2
ENV DB_PATH=/app/data/banco

EXPOSE 8080

CMD ["catalina.sh", "run"]

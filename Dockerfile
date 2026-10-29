# ============================================================
#  Pokedex API - imagem Docker (aula A44 - Deploy)
#
#  Etapa 1: compila o projeto com Maven + JDK 21
#  Etapa 2: imagem final apenas com o JRE + o .jar
#
#  Build local:
#     docker build -t pokedex-api .
#     docker run -p 8080:8080 pokedex-api
# ============================================================

# ---------------------------- build ----------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# primeiro so o pom: aproveita o cache das dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# depois o codigo
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --------------------------- runtime ---------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/pokedex-api-0.0.1-SNAPSHOT.jar app.jar

# O Render injeta a variavel PORT; localmente o padrao e 8080.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]

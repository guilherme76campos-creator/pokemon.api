# Conceitos aplicados — aula por aula

Este documento explica **cada conceito dos PDFs A39–A44** e mostra **onde** ele está no projeto
e **por que** foi feito assim. Serve para estudar e para responder às perguntas do professor.

---

## 0. A arquitetura em camadas (o coração da atividade)

O material pede quatro classes: `PokemonTO`, `PokemonDAO`, `PokemonBO` e `PokemonResource`.
Cada uma tem **uma responsabilidade** e a requisição atravessa todas nesta ordem:

```
                       JSON (HTTP)
                           │
                           ▼
        ┌──────────────────────────────────────┐
        │  PokemonResource  (@RestController)  │  recebe a requisição, escolhe o verbo,
        │  resource/PokemonResource.java       │  devolve ResponseEntity + status code
        └──────────────────┬───────────────────┘
                           │ chama
                           ▼
        ┌──────────────────────────────────────┐
        │  PokemonBO  (@Service)               │  REGRAS DE NEGÓCIO
        │  bo/PokemonBO.java                   │  (data no passado, nível 1..100, nome único)
        └──────────────────┬───────────────────┘
                           │ chama
                           ▼
        ┌──────────────────────────────────────┐
        │  PokemonDAO  (@Repository)           │  SQL (PreparedStatement, ResultSet)
        │  dao/PokemonDAO.java                 │
        └──────────────────┬───────────────────┘
                           │ usa
                           ▼
        ┌──────────────────────────────────────┐
        │  ConnectionFactory  (Singleton)      │  abre a conexão com o banco
        │  dao/ConnectionFactory.java          │
        └──────────────────┬───────────────────┘
                           ▼
                    Banco (Oracle / H2)
```

E o `PokemonTO` é o objeto que **trafega** entre essas camadas (e vira JSON na saída).

**Por que separar?** Se amanhã o banco mudar de Oracle para PostgreSQL, só o `DAO` muda. Se a regra
de negócio mudar (ex.: nível máximo 50), só o `BO` muda. O `Resource` não sabe SQL nem regra: ele só
traduz HTTP ↔ Java.

---

## A39 — API Restful (fundamentos + primeira versão)

### REST e RESTful

- **REST** é o conjunto de *princípios* de arquitetura (recursos identificados por URI, uso dos
  verbos HTTP, comunicação sem estado).
- **RESTful** é o sistema que *aplica* esses princípios. Nossa API é RESTful porque cada pokémon é
  um **recurso** em `/pokemon` e usamos o verbo HTTP certo para cada operação.

### Verbos HTTP usados

| Verbo | No projeto | Efeito |
|-------|-----------|--------|
| `GET` | `findAll`, `findByCodigo` | consultar (não altera nada) |
| `POST` | `save` | criar um novo recurso |
| `PUT` | `update` | atualizar um recurso existente |
| `DELETE` | `delete` | remover um recurso |

### Status codes (códigos de resposta)

| Código | Quando usamos |
|--------|---------------|
| `200 OK` | GET com sucesso |
| `201 CREATED` | POST (e PUT, conforme a aula A43) com sucesso |
| `204 NO CONTENT` | DELETE com sucesso (sem corpo na resposta) |
| `400 BAD REQUEST` | dados inválidos (validação ou regra de negócio) |
| `404 NOT FOUND` | código inexistente, lista vazia ou rota errada |
| `500 INTERNAL SERVER ERROR` | erro inesperado no servidor |

Quem define o código é o `ResponseEntity` no `PokemonResource`, por exemplo:

```java
return ResponseEntity.status(HttpStatus.CREATED).body(salvo);   // 201
return ResponseEntity.status(HttpStatus.NOT_FOUND).build();     // 404 sem corpo
return ResponseEntity.noContent().build();                      // 204
return ResponseEntity.ok(lista);                                // 200
```

### Spring × Spring Boot

- **Spring** é o framework (módulos MVC, Security, Transactions…).
- **Spring Boot** é o módulo que **automatiza a configuração** e traz o **Tomcat embutido**.
  Por isso não instalamos servidor: rodamos o `main` e o Tomcat sobe junto na porta 8080
  (veja o log `Tomcat started on port 8080`). A porta está em `application.properties`:

```properties
server.port=${PORT:8080}
```

### JSON

Formato leve de troca de dados. O Spring converte automaticamente:
`PokemonTO` → JSON na resposta, e JSON → `PokemonTO` no `@RequestBody`. Não escrevemos parser nenhum.

### Projeto Maven

Gerado no **Spring Initializr** (start.spring.io) com: *Spring Web*, *Spring Boot DevTools*,
*Validation*, *Oracle Driver*. A estrutura é a do material:

- `src/main/java` → código
- `src/main/resources/application.properties` → configurações
- `pom.xml` → dependências
- `mvnw.cmd` → Maven Wrapper (não precisa instalar Maven)

### A classe `PokemonResource`

```java
@RestController                      // diz que a classe responde HTTP e devolve JSON
@RequestMapping("/pokemon")          // URI base do recurso
public class PokemonResource { ... }
```

`@GetMapping` mapeia o verbo GET, `@PostMapping` o POST, e assim por diante.

### `findAll` com `ResponseEntity`

```java
@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<List<PokemonTO>> findAll() {
    List<PokemonTO> lista = bo.findAll();
    if (lista == null || lista.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // falha → 404
    }
    return ResponseEntity.ok(lista);                                 // sucesso → 200
}
```

`produces` garante que a resposta é JSON; `ResponseEntity` permite escolher **status + corpo**.

---

## A40 — Banco de dados, `ConnectionFactory` e `save`

### JDBC puro (sem JPA)

O material ensina JDBC “na mão”: `Connection` → `PreparedStatement` → `ResultSet`.
Usamos exatamente isso. Vantagem didática: o SQL fica visível.

### `ConnectionFactory` — padrões Factory e Singleton

`dao/ConnectionFactory.java`

- **Factory**: centraliza a criação de conexões (`getConnection()`), em vez de espalhar
  `DriverManager.getConnection(...)` pelo projeto.
- **Singleton**: construtor `private` + `getInstance()` garante **uma única** fábrica na aplicação.

```java
private static ConnectionFactory instancia;

public static synchronized ConnectionFactory getInstance() {
    if (instancia == null) instancia = new ConnectionFactory();
    return instancia;
}
```

### Variáveis de ambiente (já preparando a A44)

As credenciais **não** estão no código:

```java
this.url     = valorOuPadrao(System.getenv("DB_URL"), URL_PADRAO_H2);
this.usuario = valorOuPadrao(System.getenv("DB_USER"), ...);
this.senha   = valorOuPadrao(System.getenv("DB_PASSWORD"), "");
```

### A tabela

`sql/01_ddl_oracle.sql` — igual ao padrão `DDD_REMEDIOS` do material:

```sql
CREATE TABLE DDD_POKEMONS (
    CODIGO       NUMBER GENERATED ALWAYS AS IDENTITY,   -- chave auto-incrementável
    NOME         VARCHAR2(50)  NOT NULL,
    TIPO         VARCHAR2(20)  NOT NULL,
    NIVEL        NUMBER(3)     NOT NULL,
    DATA_CAPTURA DATE          NOT NULL,
    CONSTRAINT PK_DDD_POKEMONS PRIMARY KEY (CODIGO),
    CONSTRAINT UK_DDD_POKEMONS_NOME UNIQUE (NOME)
);
```

`GENERATED ALWAYS AS IDENTITY` = o banco gera o código; **não** mandamos `codigo` no INSERT.

### `PokemonDAO.save()`

```java
private static final String SQL_INSERT =
    "INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES (?, ?, ?, ?)";

try (PreparedStatement ps = con.prepareStatement(SQL_INSERT, new String[] { "CODIGO" })) {
    ps.setString(1, pokemon.getNome());
    ...
    if (ps.executeUpdate() == 0) return null;        // falha → null
    try (ResultSet rs = ps.getGeneratedKeys()) {     // pega o CODIGO gerado
        if (rs.next()) pokemon.setCodigo(rs.getLong(1));
    }
    return pokemon;                                  // sucesso → objeto com código
}
```

O contrato do material é mantido: **sucesso devolve o objeto, falha devolve `null`**.

### Regra de negócio no `PokemonBO`

A aula pede: *“valide a data da captura que deve ser uma data no passado”*.

```java
if (pokemon.getDataCaptura().isAfter(LocalDate.now())) {
    throw new RegraNegocioException(
        "A data de captura nao pode ser uma data futura. Informe uma data no passado.");
}
```

### `PokemonResource.save()` (POST)

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<PokemonTO> save(@RequestBody @Valid PokemonTO pokemon) {
    PokemonTO salvo = bo.save(pokemon);
    if (salvo == null) return ResponseEntity.badRequest().build();      // 400
    return ResponseEntity.status(HttpStatus.CREATED).body(salvo);       // 201
}
```

- `@RequestBody` → o JSON do corpo vira objeto Java.
- `consumes` → a API só aceita `application/json`.

### Insomnia

A coleção `insomnia/pokedex-insomnia.json` já traz a request **save** configurada
(POST + Body JSON). O JSON de cadastro também está em `requests/01-cadastro.json`,
como sugere a dica da aula.

---

## A41 — Bean Validation, `findAll` no banco e `findByCodigo`

### Bean Validation no `PokemonTO`

Dependência `spring-boot-starter-validation` no `pom.xml`. As anotações validam o JSON **antes**
de entrar na regra de negócio:

```java
@NotBlank(message = "O nome do pokemon e obrigatorio")
@Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
private String nome;

@Pattern(regexp = "Fogo|Agua|...|Gelo", message = "Tipo invalido...")
private String tipo;

@NotNull @Min(1) @Max(100)
private Integer nivel;

@NotNull @Past(message = "A data de captura deve ser uma data no passado")
private LocalDate dataCaptura;
```

| Anotação | Para que serve |
|----------|----------------|
| `@NotNull` | não aceita nulo |
| `@NotEmpty` | não aceita vazio (coleções/strings) |
| `@NotBlank` | não aceita vazio nem só espaços |
| `@Size` | tamanho mínimo/máximo |
| `@Min` / `@Max` | faixa numérica |
| `@Pattern` | expressão regular |
| `@Past` / `@Future` | data no passado / futuro |

### `@Valid` no Resource

Sem `@Valid` as anotações **não são executadas**:

```java
public ResponseEntity<PokemonTO> save(@RequestBody @Valid PokemonTO pokemon)
```

### `findAll` lendo do banco

O `DAO` troca a lista “mockada” por um `SELECT` real:

```java
private static final String SQL_FIND_ALL =
    "SELECT CODIGO, NOME, TIPO, NIVEL, DATA_CAPTURA FROM DDD_POKEMONS ORDER BY CODIGO";
```

### `findByCodigo` + `@PathVariable`

```java
@GetMapping(value = "/{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<PokemonTO> findByCodigo(@PathVariable long codigo) { ... }
```

`@PathVariable` pega o trecho da URI (`/pokemon/3` → `codigo = 3`) e injeta no parâmetro.
No DAO, o valor entra como parâmetro do `PreparedStatement` (`WHERE CODIGO = ?`), o que também
**evita SQL Injection**.

---

## A42 — `delete`

```java
@DeleteMapping("/{codigo}")
public ResponseEntity<String> delete(@PathVariable long codigo) {
    if (!bo.delete(codigo)) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pokemon nao encontrado ..."); // 404
    }
    return ResponseEntity.noContent().build();                                                  // 204
}
```

No DAO:

```java
public boolean delete(long codigo) {
    ...
    ps.setLong(1, codigo);
    return ps.executeUpdate() > 0;   // true = apagou, false = não achou / erro
}
```

`executeUpdate()` devolve **quantas linhas** foram afetadas. Zero linhas = o código não existia.
O `204 No Content` é o status correto para exclusão: deu certo e não há corpo para devolver.

---

## A43 — `update`

```java
@PutMapping(value = "/{codigo}", consumes = ..., produces = ...)
public ResponseEntity<PokemonTO> update(@PathVariable long codigo,
                                        @RequestBody @Valid PokemonTO pokemon) {
    PokemonTO atualizado = bo.update(codigo, pokemon);
    if (atualizado == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);   // 201, como na aula
}
```

Pontos importantes:

1. `@PathVariable` (qual registro) **+** `@RequestBody` (novos dados) no mesmo método.
2. `@Valid` valida o corpo, igual ao POST.
3. O `BO` confere se o registro existe antes de atualizar:

```java
if (dao.findByCodigo(codigo) == null) return null;   // → 404 no Resource
```

4. O `DAO` faz `UPDATE ... WHERE CODIGO = ?` e devolve `null` se nenhuma linha foi alterada.

> **Detalhe do material:** a aula A43 define **201 Created** para atualização bem-sucedida.
> Seguimos o slide. Se o professor preferir o padrão REST (200 OK), basta trocar
> `HttpStatus.CREATED` por `HttpStatus.OK` na linha do `return` do método `update`.

---

## A44 — CORS, Build, Docker, Deploy

### O que é CORS e por que o filtro é necessário

O navegador aplica a **Same-Origin Policy**: uma página em `http://localhost:5500` **não pode**
chamar `http://localhost:8080` (porta diferente = origem diferente) sem autorização explícita.
Sem o filtro, o console mostra `blocked by CORS policy` e a chamada nem chega na API.

Antes de um POST/PUT/DELETE com JSON, o navegador envia um **preflight** `OPTIONS` perguntando se
pode. O filtro responde a esse `OPTIONS` e adiciona os cabeçalhos de permissão nas respostas.

`resource/CorsFilter.java`:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)          // roda antes de tudo
public class CorsFilter implements Filter {
    public void doFilter(...) {
        resposta.setHeader("Access-Control-Allow-Origin", "*");
        resposta.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        resposta.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");

        if ("OPTIONS".equalsIgnoreCase(requisicao.getMethod())) {
            resposta.setStatus(HttpServletResponse.SC_OK);
            return;                          // responde o preflight e encerra
        }
        chain.doFilter(request, response);
    }
}
```

Em produção, troque `"*"` pelo domínio do front-end (ex.: `https://minha-pokedex.onrender.com`).

### Build (`.jar` executável)

```powershell
.\mvnw.cmd clean -DskipTests package
java -jar target\pokedex-api-0.0.1-SNAPSHOT.jar
```

O `spring-boot-maven-plugin` “reempacota” o jar com o Tomcat e todas as dependências dentro
(por isso ele tem ~31 MB e roda com um `java -jar`).

### Dockerfile

Duas etapas (*multi-stage*):

1. `maven:3.9-eclipse-temurin-21` → compila e gera o `.jar`
2. `eclipse-temurin:21-jre` → imagem final só com o JRE + o `.jar` (menor e mais segura)

### Variáveis de ambiente

Nenhuma senha no código. `DB_URL`, `DB_USER` e `DB_PASSWORD` são lidas em tempo de execução
(`System.getenv`), como o slide mostra. No Render elas são cadastradas em **Environment**.

### Git + GitHub + Render

`git init` → `add` → `commit` → `push` → no Render, **New + → Blueprint** (o `render.yaml` já
descreve o serviço) → deploy. A API fica em `https://nome-do-servico.onrender.com`.

---

## Extras (além do material, mas que ajudam na nota)

| Extra | Arquivo | Por quê |
|-------|---------|---------|
| `GlobalExceptionHandler` | `exception/GlobalExceptionHandler.java` | transforma erros em JSON limpo e mantém os status codes; sem ele o 400 vem com um corpo técnico enorme |
| `RegraNegocioException` | `bo/RegraNegocioException.java` | separa “erro de regra” de “erro de banco”, permitindo mensagem clara no 400 |
| H2 em modo Oracle | `config/DatabaseBootstrap.java` | permite rodar/testar/deployar sem credenciais; o código de produção continua sendo JDBC/Oracle |
| 15 testes JUnit | `src/test/.../PokemonResourceTests.java` | prova que todos os verbos e erros funcionam |
| Front-end | `frontend/index.html` | demonstra o CORS de verdade (aula A44) |
| Script de teste | `scripts/testar-api.ps1` | 16 casos em um comando, ótimo para a apresentação |

---

## Resumo de 30 segundos (para falar na apresentação)

> “É uma API RESTful em Spring Boot 4.1.1 com Java 21, organizada em camadas TO, DAO, BO e Resource.
> O Resource expõe `/pokemon` com os cinco endpoints — GET, GET por código, POST, PUT e DELETE — e
> devolve os status 200/201/204/400/404 conforme o material. O BO concentra as regras: data de
> captura no passado, nível de 1 a 100 e nome único. O DAO usa JDBC puro com `PreparedStatement`, e
> a `ConnectionFactory` é um Singleton que lê as credenciais de variáveis de ambiente. Os dados de
> entrada são validados com Bean Validation (`@Valid`), há um `CorsFilter` para o front-end, o build
> gera um `.jar` e o deploy está no Render via Docker.”

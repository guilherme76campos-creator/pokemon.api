# Pokédex API — Spring Boot + REST

API RESTful de cadastro de pokémons (Pokedex), feita para a atividade **"Praticando…"** das aulas
**A39 a A44 — API Restful (Domain Driven Design Using Java)**.

Implementa **todas** as funcionalidades pedidas nas seis aulas:

| Aula | Conteúdo | Onde está no projeto |
|------|----------|----------------------|
| A39 | REST, verbos HTTP, status codes, Spring Boot, JSON, TO/DAO/BO/Resource, `findAll` | `to/`, `dao/`, `bo/`, `resource/PokemonResource.java` |
| A40 | Oracle, `ConnectionFactory` (Factory + Singleton), tabela, `save` (POST 201/400) | `dao/ConnectionFactory.java`, `dao/PokemonDAO.save`, `sql/01_ddl_oracle.sql` |
| A41 | Bean Validation, `findAll` no banco, `findByCodigo` (GET `/{codigo}`) | anotações no `PokemonTO`, `PokemonResource.findByCodigo` |
| A42 | `delete` (DELETE `/{codigo}` → 204/404) | `PokemonDAO.delete`, `PokemonResource.delete` |
| A43 | `update` (PUT `/{codigo}` + `@Valid`) | `PokemonDAO.update`, `PokemonResource.update` |
| A44 | `CorsFilter`, Build `.jar`, Dockerfile, variáveis de ambiente, Git, Deploy | `resource/CorsFilter.java`, `Dockerfile`, `render.yaml` |

> Explicação conceito por conceito: **[docs/CONCEITOS.md](docs/CONCEITOS.md)**
> Roteiro para apresentar ao professor: **[docs/ROTEIRO-APRESENTACAO.md](docs/ROTEIRO-APRESENTACAO.md)**

---

## 1. Pré-requisitos

- **JDK 21** (o projeto usa `java.version=21`)
- Nada de Maven instalado: o projeto já vem com o **Maven Wrapper** (`mvnw.cmd`)
- Opcional: Insomnia / Postman, Node.js (só para servir o front-end)

Conferir se o Java está disponível:

```powershell
java -version
```

Se aparecer *"java não é reconhecido"*, aponte o `JAVA_HOME` para a sua JDK (nesta máquina ela está em
`C:\Users\Gamer\.jdks\ms-21.0.10`):

```powershell
$env:JAVA_HOME = "C:\Users\Gamer\.jdks\ms-21.0.10"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Para valer em todas as janelas: Painel de Controle → Variáveis de Ambiente → crie `JAVA_HOME` com o
caminho da JDK e acrescente `%JAVA_HOME%\bin` no `Path`.

---

## 2. Como rodar

### Opção A — IntelliJ (mais simples)

1. **File → Open** e escolha a pasta `pokedex-api`
2. Espere o IntelliJ indexar o Maven
3. Abra `src/main/java/br/com/fiap/pokedex/PokedexApiApplication.java`
4. Clique no ▶ verde (Run)
5. No console deve aparecer: `Tomcat started on port 8080` e `Banco de demonstracao inicializado com 8 pokemons`

### Opção B — Terminal (dentro da pasta do projeto)

```powershell
$env:JAVA_HOME = "C:\Users\Gamer\.jdks\ms-21.0.10"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

.\mvnw.cmd spring-boot:run
```

Ou gerando o `.jar` (exatamente como na aula A44) e executando:

```powershell
.\mvnw.cmd clean -DskipTests package
java -jar target\pokedex-api-0.0.1-SNAPSHOT.jar
```

Para parar: `Ctrl + C`.

> A aplicação sobe em **http://localhost:8080**.

---

## 3. Como testar (sem erro)

### 3.1 No navegador (só GET)

Abra: **http://localhost:8080/pokemon**

```json
[{"codigo":1,"nome":"Bulbasaur","tipo":"Planta","nivel":5,"dataCaptura":"2024-03-10"}, ...]
```

### 3.2 Insomnia / Postman (todos os verbos)

1. Insomnia → **Create → Request collection** → nome `Pokedex API`
2. Menu da collection → **Import** → escolha `insomnia/pokedex-insomnia.json`
3. Já vêm prontas as 5 requisições: `findAll`, `findByCodigo`, `save`, `update`, `delete`
4. A URL base fica na variável de ambiente `base_url` (`http://localhost:8080`).
   Para testar o deploy, troque por `https://sua-api.onrender.com`

### 3.3 Arquivo `.http` (VS Code REST Client / IntelliJ HTTP Client)

Abra `requests/pokedex.http` e clique em **Send Request** em cada bloco `###`.

### 3.4 Script automático (recomendado para a apresentação)

Com a API rodando, em outro terminal:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\testar-api.ps1
```

Ele testa **16 casos** (sucesso e erro), mostra o status de cada um e termina com
`RESULTADO: 16 testes, TODOS PASSARAM`.

Para testar o deploy:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\testar-api.ps1 -BaseUrl https://sua-api.onrender.com
```

### 3.5 Front-end (demonstra o CORS da aula A44)

```powershell
node scripts\servir-frontend.js
```

Depois abra **http://localhost:5500**. O front roda na porta 5500 e a API na 8080 — origens
diferentes, exatamente o cenário que exige o `CorsFilter`. Na tela você pode listar, cadastrar,
editar e excluir, e o painel preto embaixo mostra o status HTTP de cada chamada.

> Também funciona abrindo o `frontend/index.html` direto (duplo clique), mas aí o navegador usa a
> origem `file://`. O modo com `node` é o mais fiel ao que o professor pede.

### 3.6 Testes automatizados (JUnit)

```powershell
.\mvnw.cmd test
```

Resultado esperado: `Tests run: 15, Failures: 0, Errors: 0`.

---

## 4. Endpoints

Base: `/pokemon`

| Verbo | URI | Sucesso | Erro | Descrição |
|-------|-----|---------|------|-----------|
| GET | `/pokemon` | **200** + lista | **404** (pokedex vazia) | `findAll` |
| GET | `/pokemon/{codigo}` | **200** + objeto | **404** | `findByCodigo` |
| POST | `/pokemon` | **201** + objeto criado | **400** | `save` |
| PUT | `/pokemon/{codigo}` | **201** + objeto atualizado | **400** / **404** | `update` |
| DELETE | `/pokemon/{codigo}` | **204** (sem corpo) | **404** | `delete` |

Corpo do pokémon (JSON):

```json
{
  "nome": "Eevee",
  "tipo": "Normal",
  "nivel": 18,
  "dataCaptura": "2025-02-14"
}
```

Regras de negócio / validação:

- `nome`: obrigatório, 2 a 50 caracteres, **não pode repetir**
- `tipo`: obrigatório e deve ser um dos 18 tipos (Fogo, Água, Planta, Elétrico, …)
- `nivel`: obrigatório, **1 a 100**
- `dataCaptura`: obrigatória, formato `AAAA-MM-DD`, **deve estar no passado** (aula A40)

Exemplo de erro 400:

```json
{
  "timestamp": "2026-10-29T15:56:12.2832905",
  "status": 400,
  "erro": "Erro de validacao",
  "mensagem": "Um ou mais campos estao invalidos.",
  "campos": { "nivel": "O nivel maximo e 100" }
}
```

---

## 5. Banco de dados: H2 (demonstração) e Oracle (aula A40)

O projeto foi escrito **em JDBC puro** (DAO + `ConnectionFactory`), como no material. Só muda a URL:

| Cenário | Configuração | O que acontece |
|---------|--------------|----------------|
| Sem variáveis (padrão) | nada | Usa **H2 em memória no modo Oracle**: cria `DDD_POKEMONS` e insere 8 pokémons de exemplo |
| Oracle (FIAP) | `DB_URL`, `DB_USER`, `DB_PASSWORD` | Usa o Oracle de verdade; a tabela precisa existir |

Por que o H2? Porque o **Render (plano free) não oferece um Oracle gerenciado**, e assim o projeto
sobe e é testado em qualquer lugar sem credenciais. O SQL é o mesmo (`MODE=Oracle`), e a
`ConnectionFactory` já está pronta para o Oracle — basta informar as variáveis de ambiente.

### Rodando contra o Oracle

1. No **Oracle SQL Developer**, conectado ao seu usuário, execute `sql/01_ddl_oracle.sql`
2. No terminal, antes de subir a API:

```powershell
$env:DB_URL      = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:DB_USER     = "RM123456"
$env:DB_PASSWORD = "010190"
.\mvnw.cmd spring-boot:run
```

O log deve mostrar `Banco de dados em uso: jdbc:oracle:thin:...`.
Se aparecer `ORA-00942: table or view does not exist`, o script do passo 1 não foi executado.

---

## 6. Deploy no Render

1. Crie um repositório no GitHub e envie o projeto:

```powershell
git init
git add .
git commit -m "Pokedex API - FIAP"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/pokedex-api.git
git push -u origin main
```

2. Em [render.com](https://render.com): **New + → Blueprint** (ou **Web Service**) e escolha o repositório.
   O arquivo `render.yaml` já configura tudo (Docker, plano free, health check em `/pokemon`).
3. Se for usar Oracle, cadastre em **Environment**: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
   Se deixar em branco, sobe com o H2 de demonstração.
4. Aguarde o build. A URL fica algo como `https://pokedex-api-xxxx.onrender.com`.
5. Teste:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\testar-api.ps1 -BaseUrl https://pokedex-api-xxxx.onrender.com
```

> No plano free o serviço "dorme" após ~15 min sem acesso. A primeira chamada pode levar ~50 s
> (cold start). Isso é normal — avise o professor antes de demonstrar, ou faça uma chamada para
> "acordar" a API 1 minuto antes da apresentação.

---

## 7. Estrutura do projeto

```
pokedex-api/
├─ src/main/java/br/com/fiap/pokedex/
│  ├─ PokedexApiApplication.java      # main
│  ├─ to/PokemonTO.java               # entidade + Bean Validation (A39, A41)
│  ├─ dao/ConnectionFactory.java      # Factory + Singleton + env vars (A40, A44)
│  ├─ dao/PokemonDAO.java             # SQL: findAll, findByCodigo, save, update, delete
│  ├─ bo/PokemonBO.java               # regras de negócio
│  ├─ bo/RegraNegocioException.java
│  ├─ resource/PokemonResource.java   # endpoints REST
│  ├─ resource/CorsFilter.java        # CORS (A44)
│  ├─ exception/GlobalExceptionHandler.java
│  └─ config/DatabaseBootstrap.java   # cria tabela/dados no H2 de demonstração
├─ src/main/resources/
│  ├─ application.properties
│  └─ db/schema-h2.sql, db/data-h2.sql
├─ src/test/java/.../PokemonResourceTests.java   # 14 testes de integração
├─ frontend/index.html                # front-end (A44)
├─ insomnia/pokedex-insomnia.json     # coleção pronta
├─ requests/pokedex.http              # requisições prontas
├─ requests/01-cadastro.json          # JSON de cadastro (dica da aula A40)
├─ scripts/testar-api.ps1             # teste automático dos 16 casos
├─ scripts/servir-frontend.js         # servidor do front (porta 5500)
├─ sql/01_ddl_oracle.sql              # CREATE TABLE para o Oracle
├─ Dockerfile
└─ render.yaml
```

---

## 8. Problemas comuns

| Sintoma | Causa / solução |
|---------|-----------------|
| `java não é reconhecido` | Configure o `JAVA_HOME` (seção 1) |
| `Port 8080 was already in use` | Já existe uma API rodando. Feche a outra janela ou use `--server.port=8081` |
| `GET /pokemon` devolve 404 | A pokedex está vazia — é o comportamento da aula A39. Cadastre com POST |
| `POST` devolve 400 com `dataCaptura` | A data está no futuro. Use uma data passada |
| `POST` devolve 400 com `nome` | Nome repetido ou vazio |
| `blocked by CORS policy` no navegador | A API está parada ou a URL no front está errada. Confira se `CorsFilter` está ativo |
| `ORA-00942` | Rode `sql/01_ddl_oracle.sql` no SQL Developer |
| Render demora na 1ª chamada | Cold start do plano free (normal) |

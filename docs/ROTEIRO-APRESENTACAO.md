# Roteiro de apresentação (10–15 min)

O professor pediu: **apresentar a API do pokémon (a partir do deploy) funcionando com todas as
requisições**, podendo ser pelo Insomnia ou pelo front-end. Este roteiro cobre os dois.

---

## Antes de começar (5 minutos antes)

- [ ] **Acorde a API no Render**: abra `https://pokedex-api-uscw.onrender.com/pokemon` no navegador.
      No plano free ela “dorme”; a primeira chamada pode levar ~50 s. Faça isso antes da aula.
- [ ] Deixe abertas as abas: API no navegador, Insomnia, `http://localhost:5500` (front).
- [ ] Deixe o IntelliJ aberto na árvore de pacotes (`to`, `dao`, `bo`, `resource`).
- [ ] Tenha o `README.md` e o `docs/CONCEITOS.md` à mão para consulta.
- [ ] Rode uma vez o script para garantir que está tudo verde:
      `powershell -ExecutionPolicy Bypass -File scripts\testar-api.ps1 -BaseUrl https://pokedex-api-uscw.onrender.com`

---

## 1. Contextualizar (1 min)

> “É a atividade *Praticando* das aulas A39 a A44. Fiz uma API RESTful de Pokédex em Spring Boot
> 4.1.1 com Java 21, usando a arquitetura em camadas TO, DAO, BO e Resource, Bean Validation,
> `ConnectionFactory` com Factory e Singleton, `CorsFilter`, build em `.jar` e deploy no Render.”

---

## 2. Mostrar a arquitetura (2 min)

Abra a árvore de pacotes e diga, apontando:

| Pacote | Frase |
|--------|-------|
| `to` | “`PokemonTO` representa a tabela `DDD_POKEMONS` e carrega as validações do Bean Validation.” |
| `dao` | “`PokemonDAO` é o único que tem SQL. Usa `PreparedStatement` e devolve `null` em caso de falha, como no material.” |
| `dao` | “`ConnectionFactory` é Singleton (construtor privado + `getInstance`) e Factory de conexões; lê `DB_URL`, `DB_USER` e `DB_PASSWORD` do ambiente.” |
| `bo` | “`PokemonBO` tem as regras: data de captura no passado, nível de 1 a 100 e nome único.” |
| `resource` | “`PokemonResource` mapeia os verbos HTTP e devolve `ResponseEntity` com o status certo.” |

Fluxo para citar: **JSON → Resource → BO → DAO → Banco** e a volta.

---

## 3. Subir a aplicação (1 min)

No IntelliJ, rode `PokedexApiApplication` (ou `.\mvnw.cmd spring-boot:run`).

Destaque no log:

```
Tomcat started on port 8080 (http)
Banco de dados em uso: jdbc:h2:mem:pokedex;MODE=Oracle;...
Banco de demonstracao inicializado com 8 pokemons de exemplo.
```

> “O Tomcat é embutido — no Spring Boot o servidor vem dentro da aplicação, diferente das
> aplicações web tradicionais.”

Se estiver usando o Oracle, mostre a linha `Banco de dados em uso: jdbc:oracle:thin:...`.

---

## 4. As requisições no Insomnia (4 min)

Importe `insomnia/pokedex-insomnia.json` (Create → Import). Rode na ordem e **comente o status**:

| # | Request | Status esperado | O que dizer |
|---|---------|-----------------|-------------|
| 1 | `findAll` | **200** | “GET traz a coleção de pokémons em JSON.” |
| 2 | `findByCodigo` | **200** | “O `@PathVariable` pega o código da URI.” |
| 3 | `findByCodigo` com `/999999` | **404** | “Código inexistente → 404.” |
| 4 | `save` | **201** | “POST com `@RequestBody` + `@Valid`; o `CODIGO` foi gerado pelo banco.” |
| 5 | `save` com `"dataCaptura": "2099-01-01"` | **400** | “Regra de negócio da A40: data de captura no passado. Veja a mensagem em `campos`.” |
| 6 | `save` com `"nivel": 150` | **400** | “Bean Validation da A41 (`@Max(100)`).” |
| 7 | `save` repetindo um nome | **400** | “Regra de negócio do BO: nome único.” |
| 8 | `update` | **201** | “PUT altera o registro do código informado.” |
| 9 | `delete` | **204** | “204 No Content: apagou e não há corpo.” |
| 10 | `delete` de novo | **404** | “Já não existe.” |

> Dica: no Insomnia, mostre a aba **Headers** de qualquer resposta com o
> `Access-Control-Allow-Origin: *` — é o `CorsFilter`.

---

## 5. Front-end e CORS (2 min)

Com a API rodando:

```powershell
node scripts\servir-frontend.js
```

Abra `http://localhost:5500`.

> “O front está na porta 5500 e a API na 8080 — **origens diferentes**. Pela política de mesma
> origem o navegador bloquearia. O `CorsFilter` responde ao preflight `OPTIONS` e libera.
> A mensagem abaixo do formulário mostra o status HTTP de cada operação (201 no cadastro,
> 204 na exclusão).”

Ações para demonstrar: cadastrar um pokémon, editar o nível, excluir.

Se quiser **mostrar o erro de CORS**, comente a anotação `@Component` do `CorsFilter`, reinicie e
recarregue a página: o console do navegador mostra `blocked by CORS policy`. Descomente em seguida.

---

## 6. Deploy (2 min)

Abra `https://pokedex-api-uscw.onrender.com/pokemon` no navegador e depois rode:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\testar-api.ps1 -BaseUrl https://pokedex-api-uscw.onrender.com
```

> “São 16 casos: sucesso e erro de todos os verbos, mais o preflight de CORS. Todos passaram
> contra o deploy.”

Explique rapidamente o caminho: **GitHub → Render (Docker) → variáveis de ambiente**.
O `Dockerfile` tem duas etapas (build com Maven, runtime só com JRE) e o `render.yaml` descreve o serviço.

---

## 7. Perguntas prováveis do professor

**“Por que o H2 e não o Oracle?”**
> “A `ConnectionFactory` e o driver `ojdbc11` estão prontos para o Oracle — basta definir `DB_URL`,
> `DB_USER` e `DB_PASSWORD`; o SQL é o mesmo e a tabela é criada por `sql/01_ddl_oracle.sql`.
> No Render o plano free não oferece Oracle gerenciado, então usei H2 em **modo Oracle**
> (`MODE=Oracle`) para o deploy funcionar sem credenciais. Localmente eu rodo no Oracle normalmente.”

**“Onde está a regra de negócio?”**
> “No `PokemonBO`, nunca no Resource nem no DAO. Ex.: `validar()` checa data futura, nível e nome repetido.”

**“Por que `ResponseEntity`?”**
> “Para controlar status code + corpo + headers da resposta. Ex.: 201 com o objeto criado, 204 sem corpo.”

**“O que acontece se eu mandar `nivel: 150`?”**
> “O `@Valid` barra antes do BO: o `GlobalExceptionHandler` devolve 400 com `campos.nivel` explicando.”

**“Por que `PreparedStatement` e não concatenar string?”**
> “Evita SQL Injection e trata tipos (data, número) corretamente.”

**“O que é o preflight?”**
> “O `OPTIONS` que o navegador envia antes de POST/PUT/DELETE com JSON. O `CorsFilter` responde 200
> com os cabeçalhos `Access-Control-Allow-*`.”

**“Por que o DELETE devolve 204 e não 200?”**
> “204 No Content é o padrão REST para exclusão bem-sucedida: não há representação para devolver.”

**“Por que o PUT devolve 201?”**
> “É o que a aula A43 define. O padrão REST usaria 200; a troca é uma linha no `PokemonResource`.”

---

## 8. Checklist final

- [ ] GET lista → 200
- [ ] GET por código → 200 e 404
- [ ] POST → 201, e 400 nos três casos inválidos
- [ ] PUT → 201 (e 404 para código inexistente)
- [ ] DELETE → 204 e 404
- [ ] CORS visível (front na 5500 + header `Access-Control-Allow-Origin`)
- [ ] Deploy respondendo
- [ ] `.\mvnw.cmd test` com 15 testes verdes (se pedirem)

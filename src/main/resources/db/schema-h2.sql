-- ============================================================
-- Pokedex - tabela de demonstracao (H2 em memoria, MODE=Oracle)
-- Mesma estrutura do script Oracle (sql/01_ddl_oracle.sql).
-- ============================================================

CREATE TABLE IF NOT EXISTS DDD_POKEMONS (
    CODIGO       NUMBER(10) GENERATED ALWAYS AS IDENTITY,
    NOME         VARCHAR2(50)  NOT NULL,
    TIPO         VARCHAR2(20)  NOT NULL,
    NIVEL        NUMBER(3)     NOT NULL,
    DATA_CAPTURA DATE          NOT NULL,
    CONSTRAINT PK_DDD_POKEMONS PRIMARY KEY (CODIGO),
    CONSTRAINT UK_DDD_POKEMONS_NOME UNIQUE (NOME)
);

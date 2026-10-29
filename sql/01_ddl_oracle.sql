-- ============================================================
-- Pokedex - FIAP / Domain Driven Design Using Java
-- Script para o Oracle SQL Developer (aula A40)
-- Execute conectado ao seu usuario do Oracle (ex.: RM123456)
-- ============================================================

CREATE TABLE DDD_POKEMONS (
    CODIGO       NUMBER GENERATED ALWAYS AS IDENTITY,
    NOME         VARCHAR2(50)  NOT NULL,
    TIPO         VARCHAR2(20)  NOT NULL,
    NIVEL        NUMBER(3)     NOT NULL,
    DATA_CAPTURA DATE          NOT NULL,
    CONSTRAINT PK_DDD_POKEMONS PRIMARY KEY (CODIGO),
    CONSTRAINT UK_DDD_POKEMONS_NOME UNIQUE (NOME)
);

-- Dados de exemplo (opcional). As datas de captura devem ser passadas.
INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES ('Bulbasaur', 'Planta', 5, DATE '2024-03-10');
INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES ('Charmander', 'Fogo', 8, DATE '2024-05-22');
INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES ('Squirtle', 'Água', 7, DATE '2024-07-04');
INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES ('Pikachu', 'Elétrico', 12, DATE '2024-09-15');

COMMIT;

-- Conferir:
-- SELECT * FROM DDD_POKEMONS;

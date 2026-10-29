package br.com.fiap.pokedex.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import br.com.fiap.pokedex.dao.ConnectionFactory;

/**
 * Inicializacao do banco de DEMONSTRACAO (H2 em memoria, modo Oracle).
 *
 * - Se DB_URL aponta para o Oracle (aula A40), este componente nao faz nada:
 *   a tabela DDD_POKEMONS deve existir no Oracle (script sql/01_ddl_oracle.sql).
 * - Se nao existe DB_URL, o projeto sobe com H2 em memoria e este componente
 *   cria a tabela e insere alguns pokemons de exemplo, para a API ja responder
 *   com dados no primeiro teste.
 */
@Component
public class DatabaseBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);

    private final ConnectionFactory factory = ConnectionFactory.getInstance();

    @Override
    public void run(String... args) throws Exception {
        log.info("Banco de dados em uso: {}", factory.getUrl());

        if (!factory.isH2()) {
            log.info("Banco externo (Oracle) detectado. A tabela DDD_POKEMONS deve ser criada pelo script sql/01_ddl_oracle.sql.");
            return;
        }

        executarScript("db/schema-h2.sql");

        if (contarPokemons() == 0) {
            executarScript("db/data-h2.sql");
            log.info("Banco de demonstracao inicializado com {} pokemons de exemplo.", contarPokemons());
        }
    }

    /** Le um arquivo .sql do classpath e executa comando por comando. */
    private void executarScript(String caminho) throws IOException, SQLException {
        String conteudo;
        try (InputStream in = new ClassPathResource(caminho).getInputStream()) {
            conteudo = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // remove linhas de comentario (--) para nao quebrar a divisao por ";"
        String semComentarios = Arrays.stream(conteudo.split("\\R"))
                .filter(linha -> !linha.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));

        try (Connection con = factory.getConnection();
             Statement st = con.createStatement()) {

            for (String comando : semComentarios.split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando.trim());
                }
            }
        }
    }

    private long contarPokemons() throws SQLException {
        try (Connection con = factory.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DDD_POKEMONS")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}

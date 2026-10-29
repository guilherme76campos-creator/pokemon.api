package br.com.fiap.pokedex.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes (aula A40 / A44).
 *
 * Aplica dois padroes de projeto:
 *  - FACTORY   : centraliza a criacao de objetos Connection;
 *  - SINGLETON : garante uma unica instancia da fabrica em toda a aplicacao.
 *
 * As credenciais NAO ficam no codigo (aula A44 - variaveis de ambiente).
 * Elas sao lidas das variaveis:
 *    DB_URL      -> ex.: jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
 *    DB_USER     -> ex.: RM123456
 *    DB_PASSWORD -> ex.: 010190
 *
 * Se DB_URL nao for informada, a aplicacao sobe com o banco de demonstracao
 * H2 em memoria, no modo de compatibilidade Oracle (mesmo SQL, mesmas tabelas).
 */
public final class ConnectionFactory {

    /** Banco de demonstracao: em memoria, modo Oracle. */
    private static final String URL_PADRAO_H2 =
            "jdbc:h2:mem:pokedex;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true";

    /** Instancia unica (Singleton). */
    private static ConnectionFactory instancia;

    private final String url;
    private final String usuario;
    private final String senha;
    private final boolean h2;

    private ConnectionFactory() {
        this.url = valorOuPadrao(System.getenv("DB_URL"), URL_PADRAO_H2);
        this.h2 = this.url.startsWith("jdbc:h2");
        this.usuario = valorOuPadrao(System.getenv("DB_USER"), h2 ? "sa" : "");
        this.senha = valorOuPadrao(System.getenv("DB_PASSWORD"), "");

        try {
            Class.forName(h2 ? "org.h2.Driver" : "oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driver JDBC nao encontrado para a URL: " + url
                    + ". Verifique a dependencia no pom.xml.", e);
        }
    }

    /** Ponto de acesso ao Singleton. */
    public static synchronized ConnectionFactory getInstance() {
        if (instancia == null) {
            instancia = new ConnectionFactory();
        }
        return instancia;
    }

    /** Abre uma nova conexao com o banco configurado. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** true quando estamos usando o banco de demonstracao H2. */
    public boolean isH2() {
        return h2;
    }

    /** URL em uso (sem usuario/senha) - util para logs de inicializacao. */
    public String getUrl() {
        return url;
    }

    private static String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}

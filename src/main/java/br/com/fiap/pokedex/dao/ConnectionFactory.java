package br.com.fiap.pokedex.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final String URL_PADRAO_H2 =
            "jdbc:h2:mem:pokedex;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true";

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

    public static synchronized ConnectionFactory getInstance() {
        if (instancia == null) {
            instancia = new ConnectionFactory();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

    public boolean isH2() {
        return h2;
    }

    public String getUrl() {
        return url;
    }

    private static String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}

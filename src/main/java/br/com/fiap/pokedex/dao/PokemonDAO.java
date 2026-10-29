package br.com.fiap.pokedex.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.fiap.pokedex.to.PokemonTO;

/**
 * DAO = Data Access Object (aulas A39 a A43).
 *
 * Unica camada que conversa com o banco. Nao conhece regra de negocio:
 * apenas executa o SQL e devolve objetos PokemonTO.
 *
 * Contrato seguido em todas as aulas:
 *  - em caso de sucesso devolve o dado (ou true);
 *  - em caso de falha devolve null (ou false).
 */
@Repository
public class PokemonDAO {

    private static final String SQL_FIND_ALL =
            "SELECT CODIGO, NOME, TIPO, NIVEL, DATA_CAPTURA "
          + "FROM DDD_POKEMONS ORDER BY CODIGO";

    private static final String SQL_FIND_BY_CODIGO =
            "SELECT CODIGO, NOME, TIPO, NIVEL, DATA_CAPTURA "
          + "FROM DDD_POKEMONS WHERE CODIGO = ?";

    private static final String SQL_INSERT =
            "INSERT INTO DDD_POKEMONS (NOME, TIPO, NIVEL, DATA_CAPTURA) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE DDD_POKEMONS SET NOME = ?, TIPO = ?, NIVEL = ?, DATA_CAPTURA = ? "
          + "WHERE CODIGO = ?";

    private static final String SQL_DELETE =
            "DELETE FROM DDD_POKEMONS WHERE CODIGO = ?";

    // ---------------------------------------------------------------- findAll

    /**
     * A41: busca todos os registros no banco.
     * @return lista de pokemons ou null em caso de erro de SQL.
     */
    public List<PokemonTO> findAll() {
        List<PokemonTO> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no findAll: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------- findByCodigo

    /**
     * A41: busca um registro pela chave primaria.
     * @return o pokemon encontrado ou null se nao existir / der erro.
     */
    public PokemonTO findByCodigo(long codigo) {
        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_CODIGO)) {

            ps.setLong(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no findByCodigo: " + e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------- save

    /**
     * A40: insere um novo registro. O CODIGO e gerado pelo banco (identity),
     * por isso nao aparece na lista de colunas do INSERT.
     * @return o pokemon com o codigo preenchido ou null em caso de falha.
     */
    public PokemonTO save(PokemonTO pokemon) {
        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, new String[] { "CODIGO" })) {

            ps.setString(1, pokemon.getNome());
            ps.setString(2, pokemon.getTipo());
            ps.setInt(3, pokemon.getNivel());
            ps.setDate(4, java.sql.Date.valueOf(pokemon.getDataCaptura()));

            if (ps.executeUpdate() == 0) {
                return null;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pokemon.setCodigo(rs.getLong(1));
                }
            }
            return pokemon;

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no save: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------- update

    /**
     * A43: atualiza um registro existente.
     * @return o pokemon atualizado ou null se o codigo nao existir / der erro.
     */
    public PokemonTO update(PokemonTO pokemon) {
        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, pokemon.getNome());
            ps.setString(2, pokemon.getTipo());
            ps.setInt(3, pokemon.getNivel());
            ps.setDate(4, java.sql.Date.valueOf(pokemon.getDataCaptura()));
            ps.setLong(5, pokemon.getCodigo());

            if (ps.executeUpdate() == 0) {
                return null;
            }
            return pokemon;

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no update: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------- delete

    /**
     * A42: apaga um registro pelo codigo.
     * @return true se apagou, false se nao encontrou / deu erro.
     */
    public boolean delete(long codigo) {
        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setLong(1, codigo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no delete: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------ apoio ao BO

    /**
     * Regra de negocio do BO: nao permitir dois pokemons com o mesmo nome.
     * @param codigoIgnorado usado no update para nao comparar o registro com ele mesmo.
     */
    public boolean existeNome(String nome, Long codigoIgnorado) {
        String sql = "SELECT COUNT(*) FROM DDD_POKEMONS WHERE UPPER(NOME) = UPPER(?)";
        if (codigoIgnorado != null) {
            sql += " AND CODIGO <> ?";
        }

        try (Connection con = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            if (codigoIgnorado != null) {
                ps.setLong(2, codigoIgnorado);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[PokemonDAO] Erro no existeNome: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------- mapping

    /** Converte a linha atual do ResultSet em um PokemonTO. */
    private PokemonTO mapear(ResultSet rs) throws SQLException {
        PokemonTO pokemon = new PokemonTO();
        pokemon.setCodigo(rs.getLong("CODIGO"));
        pokemon.setNome(rs.getString("NOME"));
        pokemon.setTipo(rs.getString("TIPO"));
        pokemon.setNivel(rs.getInt("NIVEL"));
        pokemon.setDataCaptura(rs.getDate("DATA_CAPTURA").toLocalDate());
        return pokemon;
    }
}

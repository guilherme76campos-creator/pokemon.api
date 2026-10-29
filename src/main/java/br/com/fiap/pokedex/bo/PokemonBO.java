package br.com.fiap.pokedex.bo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fiap.pokedex.dao.PokemonDAO;
import br.com.fiap.pokedex.to.PokemonTO;

/**
 * BO = Business Object (aulas A40 a A43).
 *
 * Camada onde ficam as REGRAS DE NEGOCIO. Ela usa o DAO para persistir,
 * mas decide o que pode e o que nao pode ser gravado.
 *
 * Regras implementadas (as mesmas do material, adaptadas para pokemons):
 *  1) A data de captura deve ser uma data no passado (A40).
 *  2) O nivel deve estar entre 1 e 100.
 *  3) Nao pode existir dois pokemons com o mesmo nome.
 */
@Service
public class PokemonBO {

    private final PokemonDAO dao;

    public PokemonBO(PokemonDAO dao) {
        this.dao = dao;
    }

    public List<PokemonTO> findAll() {
        return dao.findAll();
    }

    public PokemonTO findByCodigo(long codigo) {
        return dao.findByCodigo(codigo);
    }

    /**
     * Cadastra um pokemon.
     * @return o pokemon cadastrado (com codigo) ou null se o banco falhar.
     */
    public PokemonTO save(PokemonTO pokemon) {
        validar(pokemon, null);
        pokemon.setCodigo(null); // o codigo e sempre gerado pelo banco
        return dao.save(pokemon);
    }

    /**
     * Atualiza um pokemon existente.
     * @return o pokemon atualizado ou null se o codigo nao existir / banco falhar.
     */
    public PokemonTO update(long codigo, PokemonTO pokemon) {
        if (dao.findByCodigo(codigo) == null) {
            return null; // registro nao existe -> o Resource devolve 404
        }
        validar(pokemon, codigo);
        pokemon.setCodigo(codigo);
        return dao.update(pokemon);
    }

    public boolean delete(long codigo) {
        return dao.delete(codigo);
    }

    /**
     * Regras de negocio.
     * @param codigoAtual null no cadastro; no update, o codigo do proprio registro
     *                    (para o teste de nome repetido nao comparar com ele mesmo).
     */
    private void validar(PokemonTO pokemon, Long codigoAtual) {

        // Regra 1 (A40): data de captura no passado.
        if (pokemon.getDataCaptura() != null && pokemon.getDataCaptura().isAfter(LocalDate.now())) {
            throw new RegraNegocioException(
                    "A data de captura nao pode ser uma data futura. Informe uma data no passado.");
        }

        // Regra 2: nivel entre 1 e 100.
        if (pokemon.getNivel() != null && (pokemon.getNivel() < 1 || pokemon.getNivel() > 100)) {
            throw new RegraNegocioException("O nivel do pokemon deve estar entre 1 e 100.");
        }

        // Regra 3: nome unico.
        if (pokemon.getNome() != null && dao.existeNome(pokemon.getNome(), codigoAtual)) {
            throw new RegraNegocioException(
                    "Ja existe um pokemon cadastrado com o nome '" + pokemon.getNome() + "'.");
        }
    }
}

package br.com.fiap.pokedex.bo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fiap.pokedex.dao.PokemonDAO;
import br.com.fiap.pokedex.to.PokemonTO;

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

    public PokemonTO save(PokemonTO pokemon) {
        validar(pokemon, null);
        pokemon.setCodigo(null);
        return dao.save(pokemon);
    }

    public PokemonTO update(long codigo, PokemonTO pokemon) {
        if (dao.findByCodigo(codigo) == null) {
            return null;
        }
        validar(pokemon, codigo);
        pokemon.setCodigo(codigo);
        return dao.update(pokemon);
    }

    public boolean delete(long codigo) {
        return dao.delete(codigo);
    }

    private void validar(PokemonTO pokemon, Long codigoAtual) {

        if (pokemon.getDataCaptura() != null && pokemon.getDataCaptura().isAfter(LocalDate.now())) {
            throw new RegraNegocioException(
                    "A data de captura nao pode ser uma data futura. Informe uma data no passado.");
        }

        if (pokemon.getNivel() != null && (pokemon.getNivel() < 1 || pokemon.getNivel() > 100)) {
            throw new RegraNegocioException("O nivel do pokemon deve estar entre 1 e 100.");
        }

        if (pokemon.getNome() != null && dao.existeNome(pokemon.getNome(), codigoAtual)) {
            throw new RegraNegocioException(
                    "Ja existe um pokemon cadastrado com o nome '" + pokemon.getNome() + "'.");
        }
    }
}

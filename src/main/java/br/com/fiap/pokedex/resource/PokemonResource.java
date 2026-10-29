package br.com.fiap.pokedex.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.pokedex.bo.PokemonBO;
import br.com.fiap.pokedex.to.PokemonTO;
import jakarta.validation.Valid;

/**
 * Resource = camada REST (aulas A39 a A44).
 *
 * Expoe o endpoint /pokemon e traduz cada verbo HTTP em uma chamada ao BO.
 * Nao tem regra de negocio nem SQL: apenas orquestra e devolve o status correto.
 *
 *   GET    /pokemon           -> 200 OK        | 404 NOT FOUND (pokedex vazia)
 *   GET    /pokemon/{codigo}  -> 200 OK        | 404 NOT FOUND
 *   POST   /pokemon           -> 201 CREATED   | 400 BAD REQUEST
 *   PUT    /pokemon/{codigo}  -> 201 CREATED   | 400 BAD REQUEST | 404 NOT FOUND
 *   DELETE /pokemon/{codigo}  -> 204 NO CONTENT| 404 NOT FOUND
 */
@RestController
@RequestMapping("/pokemon")
public class PokemonResource {

    private final PokemonBO bo;

    public PokemonResource(PokemonBO bo) {
        this.bo = bo;
    }

    // ------------------------------------------------------------------- GET

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PokemonTO>> findAll() {
        List<PokemonTO> lista = bo.findAll();

        if (lista == null || lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping(value = "/{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PokemonTO> findByCodigo(@PathVariable long codigo) {
        PokemonTO pokemon = bo.findByCodigo(codigo);

        if (pokemon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(pokemon);
    }

    // ------------------------------------------------------------------ POST

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PokemonTO> save(@RequestBody @Valid PokemonTO pokemon) {
        PokemonTO salvo = bo.save(pokemon);

        if (salvo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // ------------------------------------------------------------------- PUT

    @PutMapping(value = "/{codigo}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PokemonTO> update(@PathVariable long codigo,
                                            @RequestBody @Valid PokemonTO pokemon) {
        PokemonTO atualizado = bo.update(codigo, pokemon);

        if (atualizado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // A aula A43 define 201 Created para atualizacao bem-sucedida.
        return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);
    }

    // ---------------------------------------------------------------- DELETE

    @DeleteMapping("/{codigo}")
    public ResponseEntity<String> delete(@PathVariable long codigo) {
        if (!bo.delete(codigo)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Pokemon nao encontrado para o codigo " + codigo);
        }
        return ResponseEntity.noContent().build();
    }
}

package br.com.fiap.pokedex.resource;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.pokedex.dao.PokemonDAO;
import br.com.fiap.pokedex.to.PokemonTO;

/**
 * Testes de integracao da API (sobem o contexto Spring completo + H2 em memoria).
 * Rodam com:  .\mvnw.cmd test
 */
@SpringBootTest
@AutoConfigureMockMvc
class PokemonResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonDAO dao;

    private String json(String nome, String tipo, int nivel, String data) {
        return """
                {"nome":"%s","tipo":"%s","nivel":%d,"dataCaptura":"%s"}
                """.formatted(nome, tipo, nivel, data);
    }

    private String nomeUnico() {
        return "Teste" + System.nanoTime();
    }

    // ---------------------------------------------------------------- GET

    @Test
    @DisplayName("GET /pokemon deve retornar 200 e a lista de pokemons")
    void findAllDeveRetornar200() throws Exception {
        mockMvc.perform(get("/pokemon"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bulbasaur")));
    }

    @Test
    @DisplayName("GET /pokemon/1 deve retornar 200")
    void findByCodigoDeveRetornar200() throws Exception {
        mockMvc.perform(get("/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bulbasaur")));
    }

    @Test
    @DisplayName("GET /pokemon/{codigo inexistente} deve retornar 404")
    void findByCodigoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/pokemon/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /pokemon/abc deve retornar 400")
    void findByCodigoInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/pokemon/abc"))
                .andExpect(status().isBadRequest());
    }

    // --------------------------------------------------------------- POST

    @Test
    @DisplayName("POST /pokemon valido deve retornar 201")
    void saveValidoDeveRetornar201() throws Exception {
        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nomeUnico(), "Fogo", 25, "2025-03-15")))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"nivel\":25")));
    }

    @Test
    @DisplayName("POST /pokemon com data futura deve retornar 400 (regra de negocio A40)")
    void saveComDataFuturaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nomeUnico(), "Fogo", 25, "2099-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("dataCaptura")));
    }

    @Test
    @DisplayName("POST /pokemon com nivel 150 deve retornar 400 (Bean Validation A41)")
    void saveComNivelInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nomeUnico(), "Fogo", 150, "2025-03-15")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nivel")));
    }

    @Test
    @DisplayName("POST /pokemon com nome vazio deve retornar 400")
    void saveComNomeVazioDeveRetornar400() throws Exception {
        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "Fogo", 25, "2025-03-15")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nome")));
    }

    @Test
    @DisplayName("POST /pokemon com nome repetido deve retornar 400")
    void saveComNomeRepetidoDeveRetornar400() throws Exception {
        String nome = nomeUnico();

        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nome, "Fogo", 10, "2025-03-15")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nome, "Agua", 10, "2025-03-15")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Ja existe")));
    }

    @Test
    @DisplayName("POST /pokemon com JSON invalido deve retornar 400")
    void saveComJsonInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isso nao e json }"))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- PUT

    @Test
    @DisplayName("PUT /pokemon/{codigo} valido deve retornar 201")
    void updateValidoDeveRetornar201() throws Exception {
        PokemonTO pokemon = new PokemonTO(null, nomeUnico(), "Fogo", 10, LocalDate.now().minusDays(5));
        dao.save(pokemon);

        mockMvc.perform(put("/pokemon/" + pokemon.getCodigo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(pokemon.getNome() + "X", "Fogo", 30, "2025-03-16")))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"nivel\":30")));
    }

    @Test
    @DisplayName("PUT /pokemon/{codigo inexistente} deve retornar 404")
    void updateInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(put("/pokemon/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nomeUnico(), "Fogo", 30, "2025-03-16")))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------- DELETE

    @Test
    @DisplayName("DELETE /pokemon/{codigo} deve retornar 204 e depois 404")
    void deleteDeveRetornar204EDepois404() throws Exception {
        PokemonTO pokemon = new PokemonTO(null, nomeUnico(), "Agua", 12, LocalDate.now().minusDays(2));
        dao.save(pokemon);

        mockMvc.perform(delete("/pokemon/" + pokemon.getCodigo()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/pokemon/" + pokemon.getCodigo()))
                .andExpect(status().isNotFound());
    }

    // --------------------------------------------------------------- CORS

    @Test
    @DisplayName("OPTIONS /pokemon (preflight) deve retornar 200 com cabecalhos CORS")
    void preflightDeveRetornar200ComCors() throws Exception {
        mockMvc.perform(options("/pokemon")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}

package br.com.fiap.pokedex.to;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * TO = Transfer Object.
 *
 * Representa a entidade (tabela DDD_POKEMONS) e e o objeto que trafega entre
 * as camadas (Resource -> BO -> DAO) e entre a API e o cliente (JSON).
 *
 * As anotacoes de Bean Validation (aula A41) validam o JSON recebido antes
 * de o objeto chegar na regra de negocio.
 */
public class PokemonTO {

    /** Chave primaria (gerada pelo banco - NUMBER GENERATED ALWAYS AS IDENTITY). */
    private Long codigo;

    @NotBlank(message = "O nome do pokemon e obrigatorio")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    private String nome;

    @NotBlank(message = "O tipo do pokemon e obrigatorio")
    @Pattern(
        regexp = "Fogo|Agua|Água|Planta|Eletrico|Elétrico|Psiquico|Psíquico|Normal|Lutador|Veneno|Terra|Voador|Inseto|Pedra|Fantasma|Dragao|Dragão|Aco|Aço|Fada|Sombrio|Gelo",
        message = "Tipo invalido. Use: Fogo, Agua, Planta, Eletrico, Psiquico, Normal, Lutador, Veneno, Terra, Voador, Inseto, Pedra, Fantasma, Dragao, Aco, Fada, Sombrio ou Gelo"
    )
    private String tipo;

    @NotNull(message = "O nivel do pokemon e obrigatorio")
    @Min(value = 1, message = "O nivel minimo e 1")
    @Max(value = 100, message = "O nivel maximo e 100")
    private Integer nivel;

    /**
     * Regra de negocio da aula A40: a data de captura deve ser uma data no passado.
     * O @Past faz a validacao automatica; o PokemonBO repete a regra (defesa em profundidade).
     */
    @NotNull(message = "A data de captura e obrigatoria (formato AAAA-MM-DD)")
    @Past(message = "A data de captura deve ser uma data no passado")
    private LocalDate dataCaptura;

    public PokemonTO() {
    }

    public PokemonTO(Long codigo, String nome, String tipo, Integer nivel, LocalDate dataCaptura) {
        this.codigo = codigo;
        this.nome = nome;
        this.tipo = tipo;
        this.nivel = nivel;
        this.dataCaptura = dataCaptura;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public LocalDate getDataCaptura() {
        return dataCaptura;
    }

    public void setDataCaptura(LocalDate dataCaptura) {
        this.dataCaptura = dataCaptura;
    }

    @Override
    public String toString() {
        return "PokemonTO [codigo=" + codigo + ", nome=" + nome + ", tipo=" + tipo
                + ", nivel=" + nivel + ", dataCaptura=" + dataCaptura + "]";
    }
}

package br.com.fiap.pokedex.bo;

/**
 * Excecao de regra de negocio.
 *
 * Quando o PokemonBO detecta que uma regra foi violada (data de captura no
 * futuro, nivel fora de 1..100, nome repetido), ele lanca esta excecao.
 * O GlobalExceptionHandler transforma isso em HTTP 400 (Bad Request)
 * com uma mensagem clara para o cliente da API.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

package br.com.fiap.pokedex.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.fiap.pokedex.bo.RegraNegocioException;

/**
 * Tratamento global de erros.
 *
 * Sem esta classe o Spring devolveria 400 com um corpo enorme e tecnico.
 * Aqui traduzimos cada tipo de erro em um JSON simples, mantendo os status
 * codes do material:
 *   - Bean Validation / regra de negocio / JSON invalido -> 400 Bad Request
 *   - rota inexistente                                   -> 404 Not Found
 *   - qualquer erro inesperado                           -> 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean Validation: campos invalidos no JSON (aula A41). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        return ResponseEntity.badRequest().body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validacao",
                "Um ou mais campos estao invalidos.",
                campos));
    }

    /** Regras de negocio do PokemonBO. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Regra de negocio violada",
                ex.getMessage(),
                null));
    }

    /** JSON malformado ou data em formato invalido (ex.: "dataCaptura": "ontem"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "JSON invalido",
                "O corpo da requisicao nao pode ser lido. Verifique o JSON e o formato das datas (AAAA-MM-DD).",
                null));
    }

    /** Parametro de rota com tipo errado, ex.: GET /pokemon/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parametro invalido",
                "O parametro '" + ex.getName() + "' deve ser um numero inteiro.",
                null));
    }

    /** Rota que nao existe, ex.: GET /pokemons. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Rota nao encontrada",
                "Confira a URI. Os endpoints da Pokedex estao em /pokemon.",
                null));
    }

    /** Qualquer erro inesperado. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex) {
        System.err.println("[ERRO] " + ex.getClass().getName() + ": " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado no servidor.",
                null));
    }
}

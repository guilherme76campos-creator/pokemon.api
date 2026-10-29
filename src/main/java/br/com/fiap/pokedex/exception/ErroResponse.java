package br.com.fiap.pokedex.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corpo padrao de erro devolvido pela API.
 * Exemplo:
 * {
 *   "timestamp": "2026-10-29T10:15:30",
 *   "status": 400,
 *   "erro": "Erro de validacao",
 *   "mensagem": "Um ou mais campos estao invalidos.",
 *   "campos": { "nivel": "O nivel maximo e 100" }
 * }
 */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos) {
}

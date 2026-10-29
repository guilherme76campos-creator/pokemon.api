package br.com.fiap.pokedex.resource;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CORS Filter (aula A44).
 *
 * CORS = Cross-Origin Resource Sharing. O navegador bloqueia, por seguranca,
 * uma pagina de uma origem (ex.: http://localhost:3000) de chamar uma API de
 * outra origem (ex.: http://localhost:8080). Este filtro adiciona os cabecalhos
 * que autorizam essa chamada.
 *
 * Ordem: e o primeiro filtro da cadeia, para responder ao preflight (OPTIONS)
 * antes de qualquer outro processamento.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest requisicao = (HttpServletRequest) request;
        HttpServletResponse resposta = (HttpServletResponse) response;

        // Em producao troque "*" pelo dominio do seu front-end, ex.: https://minha-pokedex.onrender.com
        resposta.setHeader("Access-Control-Allow-Origin", "*");
        resposta.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        resposta.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        resposta.setHeader("Access-Control-Expose-Headers", "Location");
        resposta.setHeader("Access-Control-Max-Age", "3600");

        // Preflight: o navegador pergunta antes se pode chamar a API.
        if ("OPTIONS".equalsIgnoreCase(requisicao.getMethod())) {
            resposta.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}

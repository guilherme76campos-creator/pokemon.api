const http = require("http");
const fs = require("fs");
const path = require("path");

const PORTA = Number(process.env.PORT || 5500);
const RAIZ = path.join(__dirname, "..", "frontend");

const TIPOS = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".ico": "image/x-icon",
};

http.createServer((req, res) => {
  const url = decodeURIComponent(req.url.split("?")[0]);
  const relativo = url === "/" ? "index.html" : url.replace(/^\/+/, "");
  const arquivo = path.join(RAIZ, relativo);

  if (!arquivo.startsWith(RAIZ)) {
    res.writeHead(403).end("Acesso negado");
    return;
  }

  fs.readFile(arquivo, (erro, conteudo) => {
    if (erro) {
      res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
      res.end("Arquivo nao encontrado: " + relativo);
      return;
    }
    res.writeHead(200, {
      "Content-Type": TIPOS[path.extname(arquivo).toLowerCase()] || "application/octet-stream",
      "Cache-Control": "no-store",
    });
    res.end(conteudo);
  });
}).listen(PORTA, () => {
  console.log("Front-end da Pokedex em http://localhost:" + PORTA);
  console.log("(a API deve estar rodando em http://localhost:8080)");
});

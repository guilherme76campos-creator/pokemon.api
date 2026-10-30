# Apresentação do projeto – Pokédex API

Meu projeto consiste no desenvolvimento de uma API RESTful para o gerenciamento de uma Pokédex, utilizando Java e Spring Boot, seguindo os conceitos de Domain Driven Design (DDD) trabalhados nas aulas A39 a A44 da FIAP.

O objetivo foi desenvolver um sistema que permita cadastrar, consultar, atualizar e excluir Pokémon, utilizando os principais métodos HTTP.

**1. Estrutura do projeto**

Organizei o projeto em algumas camadas, cada uma com uma responsabilidade específica:

- **TO:** representa os dados dos Pokémon e suas validações.
- **DAO:** responsável pela comunicação com o banco de dados.
- **BO:** concentra as regras de negócio.
- **Resource:** disponibiliza os endpoints da API e recebe as requisições.

Essa organização facilita a manutenção e deixa o código mais organizado.

**2. Funcionalidades**

Implementei cinco operações principais:

- GET: listar todos os Pokémon.
- GET por código: consultar um Pokémon específico.
- POST: cadastrar um novo Pokémon.
- PUT: atualizar um Pokémon existente.
- DELETE: excluir um Pokémon.

Também implementei validações para impedir cadastros incorretos, como nomes repetidos, tipos inválidos, níveis fora do intervalo permitido e datas de captura inválidas.

**3. Banco de dados**

Utilizei JDBC para realizar a comunicação com o banco de dados.

O projeto está preparado para trabalhar com Oracle, conforme o conteúdo da aula A40, e também utiliza H2 em memória como banco de demonstração. Isso permite executar e testar a aplicação sem depender de credenciais externas.

**4. Testes e integração**

Utilizei o Insomnia para testar as requisições HTTP e verificar se as operações estavam funcionando corretamente.

Também desenvolvi um Front-end para demonstrar as funcionalidades de cadastro, consulta, edição e exclusão dos Pokémon.

Implementei o CorsFilter para permitir a comunicação entre o Front-end e a API, mesmo quando estão em origens diferentes.

**5. Deploy**

Por fim, publiquei o projeto no GitHub e realizei o deploy no Render, disponibilizando a API pela internet.

Dessa forma, é possível acessar e testar as funcionalidades sem precisar executar o projeto localmente.

**Conclusão**

Com esse projeto, consegui aplicar na prática os conceitos de API RESTful, Java, Spring Boot, JDBC, banco de dados, validações, organização em camadas, testes com Insomnia e publicação de aplicações.

O desenvolvimento também me ajudou a compreender melhor como funciona a comunicação entre Front-end, Back-end e banco de dados em uma aplicação real.

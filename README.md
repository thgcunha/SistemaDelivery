# Sistema Delivery

API REST para gerenciamento de restaurantes, pratos e pedidos, desenvolvida originalmente na disciplina de Programação Orientada a Objetos II. O projeto está sendo refatorado em etapas para compor um portfólio de desenvolvimento Java.

## Estado atual

A configuração de compilação e a execução do Maven Wrapper no Windows foram corrigidas. A API conta com tratamento padronizado de erros HTTP, validação dos campos de entrada, vínculo entre pratos e restaurantes, itens de pedido com preço histórico e testes de regressão. Ainda não há interface web nem demonstração publicada.

## Tecnologias atuais

- Java 17
- Spring Boot 3.2.9
- Spring Web e Spring Data JPA
- H2 em memória
- Lombok
- JUnit 5 e Spring Boot Test
- Maven 3.9.9 via Maven Wrapper

## Recursos implementados

- Cadastro, consulta, edição e desativação de restaurantes e pratos.
- Filtros e consultas paginadas.
- Alteração de disponibilidade de pratos.
- Criação de pedidos com quantidade por item, preço histórico e total calculado com `BigDecimal`.
- Consulta, alteração de status e cancelamento de pedidos.

## Executar localmente

Instale um **JDK 17** e configure `JAVA_HOME` para a pasta do JDK. Não é necessário instalar o Maven: o wrapper baixa a versão utilizada pelo projeto. A primeira execução precisa de acesso à internet para obter o Maven e as dependências.

Clone o repositório e entre na pasta:

```sh
git clone https://github.com/thgcunha/SistemaDelivery.git
cd SistemaDelivery
```

No Windows, com PowerShell:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```sh
sh ./mvnw clean verify
sh ./mvnw spring-boot:run
```

Com a aplicação iniciada, consulte [restaurantes](http://localhost:8080/restaurante), [pratos](http://localhost:8080/prato) ou [pedidos](http://localhost:8080/pedido). O banco começa vazio, por isso essas consultas inicialmente retornam listas vazias.

Para encerrar a aplicação, pressione `Ctrl+C` no terminal.

## Banco de desenvolvimento

O H2 roda em memória: os dados são descartados quando a aplicação é encerrada. Não é necessário instalar um banco para executar esta versão.

O [console H2](http://localhost:8080/h2-console) está disponível durante a execução local:

- JDBC URL: `jdbc:h2:mem:meubanco`
- Usuário: `sa`
- Senha: deixe em branco

As configurações estão em [`application.properties`](src/main/resources/application.properties). Essa configuração é destinada ao desenvolvimento local.

## Testes e empacotamento

O comando `clean verify` compila, executa os testes e gera o JAR em `target/`. A suíte cobre:

- Inicialização do contexto Spring.
- Respostas HTTP para recursos inexistentes, parâmetros inválidos e corpos ausentes ou malformados, usando MockMvc, services reais e H2.
- Rejeição de pedido que referencia um prato inexistente, sem persistir o pedido.
- Preservação de códigos HTTP e cabeçalhos, como `Allow` nas respostas 405.
- Resposta genérica para falhas inesperadas, sem expor detalhes internos.
- Consulta bem-sucedida de um restaurante cadastrado.
- Rejeição de dados inválidos em restaurantes, pratos e pedidos, além de um fluxo completo com dados válidos.
- Vínculo entre prato e restaurante, indisponibilidade de pratos e transições de status dos pedidos.
- Quantidade, subtotal, preço histórico e total dos itens após edição do prato.

Os testes de integração usam transações com rollback para isolar os dados de cada cenário. Os testes das demais regras de negócio serão adicionados junto às respectivas correções.

Depois do build, também é possível executar o JAR:

```sh
java -jar target/universidadeESN3-0.0.1-SNAPSHOT.jar
```

## Respostas de erro

As respostas mantêm os campos `status`, `error`, `message`, `path` e `timestamp`. Exemplo de consulta de um restaurante inexistente:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Restaurante não encontrado com o ID: 999999",
  "path": "/restaurante/999999",
  "timestamp": "2026-11-09T12:00:00"
}
```

| Código | Situação |
| --- | --- |
| 400 | Parâmetro obrigatório ausente, tipo inválido, corpo ausente ou JSON malformado |
| 404 | Restaurante, prato, pedido ou rota não encontrado |
| 405 | Método HTTP não permitido; o cabeçalho `Allow` informa os métodos aceitos |
| 409 | Prato ou restaurante indisponível, ou transição de status inválida |
| 415 | Tipo de conteúdo da requisição não suportado |
| 422 | Campo obrigatório ausente, texto acima do limite ou preço inválido |
| 500 | Falha inesperada; o cliente recebe uma mensagem genérica e os detalhes ficam no log do servidor |

Na criação de pedidos, a lista `itens` deve conter ao menos um objeto com `pratoId` e `quantidade` positiva. O servidor consulta os pratos, copia nome e preço para cada item e calcula subtotais e total. Valores, status e datas enviados pelo cliente não alteram esses dados.

```json
{
  "nomeCliente": "Ana",
  "telefoneCliente": "11999999999",
  "enderecoEntrega": "Rua das Flores, 10",
  "itens": [
    { "pratoId": 1, "quantidade": 2 }
  ]
}
```

A resposta contém `restauranteId` e `itens` com `pratoId`, `nomePrato`, `quantidade`, `precoUnitario` e `subtotal`, além de `valorTotal`. Esses valores permanecem no pedido mesmo após uma alteração no prato. O corpo antigo com `pratos: [{"id": 1}]` foi substituído por `itens`.

Ao criar um prato, informe o ID de um restaurante ativo:

```json
{"nome":"Prato de teste","preco":25.90,"restaurante":{"id":1}}
```

Pedidos aceitam apenas pratos ativos e disponíveis do mesmo restaurante. A API retorna `409 Conflict` quando um prato não está disponível ou quando uma mudança de status não é permitida. O fluxo de status é `PENDENTE → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE`. O cancelamento é permitido até `EM_PREPARO`; pedidos entregues e cancelados são finais. Repetir o mesmo status é permitido e não altera a data da entrega.

## Próximas etapas

1. Evoluir os contratos da API, a persistência e as dependências.
2. Integrar consulta de endereço por CEP.
3. Criar a interface web e o fluxo completo de pedido.
4. Adicionar rota estimada e cálculo de frete.
5. Documentar a API e publicar a demonstração.

Integração com pagamentos está fora do escopo desta evolução. A coleção Postman em `src/main/resources/collections/` é um artefato do projeto acadêmico anterior e ainda não documenta as rotas atuais.

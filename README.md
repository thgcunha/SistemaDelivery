# Sistema Delivery

**Projeto acadêmico de Programação Orientada a Objetos II (POO II)**, desenvolvido com acompanhamento e colaboração do professor e posteriormente retomado como projeto de portfólio. A aplicação reúne uma API REST em Java e Spring Boot, regras de pedidos, testes de integração e uma interface responsiva em HTML, CSS e JavaScript.

[![Validar aplicação](https://github.com/thgcunha/SistemaDelivery/actions/workflows/ci.yml/badge.svg)](https://github.com/thgcunha/SistemaDelivery/actions/workflows/ci.yml)

**Demonstração:** [abrir a interface no GitHub Pages](https://thgcunha.github.io/SistemaDelivery/). **Código:** [repositório no GitHub](https://github.com/thgcunha/SistemaDelivery).

> **Como avaliar:** no GitHub Pages, o catálogo e os pedidos funcionam em modo demonstração no navegador; o Pages não executa a API REST Spring Boot. Para testar os endpoints e as regras no servidor, execute o projeto localmente. A consulta de CEP e a estimativa de entrega podem acessar APIs públicas diretamente do navegador, conforme a disponibilidade desses serviços.

## O projeto em poucas palavras

O usuário escolhe um restaurante, consulta o cardápio, monta a sacola, informa o endereço e acompanha o pedido. No ambiente local, a interface conversa com a API Spring Boot e os dados são armazenados em H2 durante a execução. A versão publicada no GitHub Pages reproduz o fluxo no navegador, com pedidos de demonstração salvos em `localStorage`.

O projeto nasceu para praticar modelagem orientada a objetos. Na evolução para portfólio, o foco foi tornar as relações de domínio, as regras de negócio, os erros e os testes mais claros, mantendo o escopo acadêmico e sem introduzir pagamento real.

## POO aplicada ao domínio

| Conceito | Aplicação concreta |
| --- | --- |
| Modelagem de objetos | `Restaurante`, `Prato`, `Pedido` e `ItemPedido` representam as entidades do domínio. |
| Associações e composição | Um restaurante possui pratos; um pedido contém itens vinculados a ele. As relações são mapeadas com JPA. |
| Abstração por interfaces | `IRestauranteService`, `IPratoService` e `IPedidoService` definem contratos implementados pelos serviços. |
| Responsabilidades separadas | Controllers recebem requisições, services aplicam regras e repositories acessam os dados. |
| Comportamento de domínio | `StatusPedido` define quais transições de estado são permitidas. |
| Integridade do pedido | `PedidoService` valida disponibilidade, exige itens de um mesmo restaurante e calcula valores no servidor. |

## Funcionalidades

- Consulta de restaurantes e cardápios, com busca e filtros por categoria na interface.
- Sacola com quantidades e subtotal atualizado.
- Criação de pedidos com itens, preço unitário e subtotal registrados no momento da compra.
- Validação de restaurante, disponibilidade dos pratos, quantidades e limite de valor.
- Acompanhamento de `PENDENTE → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE`, com cancelamento permitido até `EM_PREPARO`.
- Consulta de CEP e estimativa informativa de distância, tempo e frete quando as APIs públicas respondem.
- Respostas de erro padronizadas e testes automatizados para fluxos e validações.

O frete estimado não é cobrado nem incluído no valor do pedido.

## Tecnologias e organização

| Área | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Bean Validation |
| Persistência local | H2 em memória, Hibernate |
| Interface | HTML, CSS, JavaScript e SVG |
| Qualidade | JUnit 5, MockMvc, Maven Wrapper, GitHub Actions |
| Publicação | GitHub Pages para a demonstração estática; Dockerfile para hospedagem opcional da API |

```text
src/main/java/com/example/universidadeESN3/
├── controller/    endpoints HTTP
├── dto/           entrada de pedidos
├── entity/        modelo de domínio
├── exception/     erros da API
├── repository/    acesso aos dados
└── service/       regras e integrações
src/main/resources/static/   interface e catálogo de demonstração
src/test/                    testes automatizados
.github/workflows/           CI e publicação da interface
```

## Executar localmente

Requisitos: **JDK 17** e `JAVA_HOME` configurado. O Maven Wrapper está incluído; não é necessário instalar Maven separadamente.

```powershell
git clone https://github.com/thgcunha/SistemaDelivery.git
cd SistemaDelivery
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, use `sh ./mvnw` no lugar de `.\mvnw.cmd`. Abra [http://localhost:8080/](http://localhost:8080/) após a inicialização. Para reiniciar, pressione `Ctrl+C` no terminal e execute apenas `.\mvnw.cmd spring-boot:run`.

O H2 é recriado a cada inicialização. Para habilitar o console durante o desenvolvimento, defina `H2_CONSOLE_ENABLED=true` e acesse `/h2-console` com URL `jdbc:h2:mem:meubanco`, usuário `sa` e senha vazia.

### Roteiro rápido de verificação

1. Selecione um restaurante e confirme que o cardápio muda.
2. Use busca e filtros; adicione pratos e ajuste as quantidades.
3. Troque de restaurante com itens na sacola e confira o aviso de limpeza.
4. Finalize um pedido, consulte-o em **Pedidos** e avance o status.
5. Reduza a janela para conferir a versão para celular.

Execute `.\mvnw.cmd clean verify` para rodar a suíte de testes. O workflow **Validar aplicação** faz a mesma verificação a cada push na `main` e em pull requests.

## API REST

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` / `POST` / `PUT` | `/restaurante` | Listar, cadastrar e atualizar restaurantes |
| `GET` | `/restaurante/{id}` | Consultar restaurante e pratos |
| `GET` / `POST` / `PUT` | `/prato` | Listar, cadastrar e atualizar pratos |
| `GET` | `/prato/restaurante/{id}` | Consultar cardápio de um restaurante |
| `GET` / `POST` | `/pedido` | Listar e criar pedidos |
| `GET` | `/pedido/{id}` | Consultar pedido |
| `PATCH` | `/pedido/{id}/status?status=CONFIRMADO` | Avançar status |
| `DELETE` | `/pedido/{id}` | Cancelar pedido |
| `GET` | `/api/cep/{cep}` | Consultar endereço |
| `GET` | `/api/entrega/estimativa?restauranteId=1&cep=01001000` | Estimar rota e frete |

Exemplo de criação de pedido:

```json
{
  "nomeCliente": "Ana",
  "telefoneCliente": "11999999999",
  "enderecoEntrega": "Praça da Sé, 10, São Paulo - SP",
  "itens": [
    { "pratoId": 1, "quantidade": 2 }
  ]
}
```

O cliente envia IDs e quantidades. O servidor busca os preços atuais, calcula o total com `BigDecimal` e devolve os itens com nome, preço unitário e subtotal. A coleção Postman antiga em `src/main/resources/collections/` pertence à versão acadêmica inicial e não representa o contrato atual.

## Integrações e limites

O CEP é consultado via [ViaCEP](https://viacep.com.br/). Quando há coordenadas, a estimativa utiliza [BrasilAPI CEP v2](https://brasilapi.com.br/) e [OSRM/OpenStreetMap](https://project-osrm.org/). O cálculo informativo é `R$ 4,90 + R$ 1,80 × distância rodoviária em km`, arredondado a centavos. Falhas dessas APIs não impedem o pedido com endereço preenchido manualmente.

Restaurantes e pratos da vitrine são fictícios. Não há autenticação, pagamento, cobrança de frete ou persistência durável na demonstração pública. O backend local usa H2 em memória, portanto reiniciar a aplicação apaga os pedidos. Este é um projeto acadêmico e de portfólio, não um serviço comercial.

## Autoria

**Thiago Gonçalves Cunha** · Trabalho Acadêmico de POO II, desenvolvido com acompanhamento e colaboração do professor e aprimorado para apresentação em portfólio.

## Demonstração e implantação

A [demonstração no GitHub Pages](https://thgcunha.github.io/SistemaDelivery/) publica a interface estática de `src/main/resources/static/`. Nesse ambiente, o catálogo vem de um arquivo JSON e os pedidos ficam no `localStorage` de cada navegador. O Pages não executa a API Spring Boot; os endpoints REST e as regras persistidas em H2 podem ser explorados ao executar a aplicação localmente.

O workflow [`pages.yml`](.github/workflows/pages.yml) publica a interface. O [`Dockerfile`](Dockerfile) prepara a API para uma hospedagem separada. A interface aceita uma URL de backend externo pelo parâmetro `?api=`, desde que a origem do Pages esteja autorizada no CORS da API. Como o H2 é volátil, uma implantação com dados duráveis exigiria banco persistente e migrações.

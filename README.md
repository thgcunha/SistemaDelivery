# Sistema Delivery

Aplicação de delivery construída com **Java 17, Spring Boot e JavaScript**. O projeto nasceu em uma disciplina de Programação Orientada a Objetos e evoluiu para um portfólio com API REST, regras de pedido, testes de integração e uma interface responsiva.

**[Abrir demonstração no GitHub Pages](https://thgcunha.github.io/SistemaDelivery/)** · **[Explorar o código](https://github.com/thgcunha/SistemaDelivery)**

> O link da demonstração ficará disponível após ativar GitHub Pages no repositório. A versão do Pages funciona em modo demonstração no navegador; os pedidos ficam no `localStorage` e não são compartilhados. A aplicação local usa a API Spring Boot e o banco H2.

## O que o sistema faz

- Exibe restaurantes, cardápios, busca e filtros por categoria.
- Permite montar uma sacola com quantidades e criar pedidos.
- Calcula subtotais e total no servidor com `BigDecimal` e guarda nome e preço dos itens no momento da compra.
- Valida disponibilidade e exige que todos os itens pertençam ao mesmo restaurante.
- Acompanha o fluxo `PENDENTE → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE`, com cancelamento até `EM_PREPARO`.
- Consulta endereço pelo CEP via [ViaCEP](https://viacep.com.br/) e estima distância, duração e frete com [BrasilAPI CEP v2](https://brasilapi.com.br/) e [OSRM/OpenStreetMap](https://github.com/Project-OSRM/osrm-backend/wiki/Demo-server), quando há coordenadas e rota disponíveis.
- Oferece respostas de erro padronizadas e testes de integração com MockMvc e H2.

O frete exibido é **informativo** e **não entra no valor do pedido**. Não há pagamento real. A fórmula da estimativa é `R$ 4,90 + R$ 1,80 × distância rodoviária em km`, arredondada a centavos. O OSRM público é um serviço de demonstração sem garantia de disponibilidade, limitado a uso razoável e não comercial.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Bean Validation |
| Dados | H2 em memória, Hibernate |
| Interface | HTML, CSS e JavaScript sem framework |
| Qualidade | JUnit 5, MockMvc, Maven Wrapper, GitHub Actions |
| Publicação | GitHub Pages para a interface; Dockerfile para hospedagem opcional do backend |

## Executar e testar localmente

Requisitos: **JDK 17** com `JAVA_HOME` configurado. O Maven Wrapper já está no repositório.

```powershell
git clone https://github.com/thgcunha/SistemaDelivery.git
cd SistemaDelivery
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, substitua `.\mvnw.cmd` por `sh ./mvnw`. Abra **[http://localhost:8080](http://localhost:8080)**. O catálogo de demonstração é carregado automaticamente. Faça um pedido, avance o status na seção **Pedidos** e teste também a versão para celular reduzindo a janela.

Para testar o modo estático do Pages na própria aplicação, abra **[http://localhost:8080/?demo](http://localhost:8080/?demo)**. Nesse modo os pedidos ficam no navegador. Para consultar um CEP sem depender das APIs externas, preencha o endereço manualmente; a criação do pedido continua disponível.

O banco local é H2 em memória e é recriado a cada inicialização. Se quiser usar o console H2 apenas durante o desenvolvimento, inicie com `H2_CONSOLE_ENABLED=true` e abra `/h2-console` com URL `jdbc:h2:mem:meubanco`, usuário `sa` e senha vazia.

## Fluxo da API

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` / `POST` / `PUT` | `/restaurante` | Listar, cadastrar e atualizar restaurantes |
| `GET` | `/restaurante/{id}` | Consultar restaurante e pratos |
| `GET` / `POST` / `PUT` | `/prato` | Listar, cadastrar e atualizar pratos |
| `GET` | `/prato/restaurante/{id}` | Cardápio por restaurante |
| `GET` / `POST` | `/pedido` | Listar e criar pedidos |
| `GET` | `/pedido/{id}` | Consultar pedido |
| `PATCH` | `/pedido/{id}/status?status=CONFIRMADO` | Avançar status |
| `DELETE` | `/pedido/{id}` | Cancelar pedido |
| `GET` | `/api/cep/{cep}` | Consultar endereço |
| `GET` | `/api/entrega/estimativa?restauranteId=1&cep=01001000` | Estimar rota e frete |

As rotas de filtros e paginação existentes continuam disponíveis nos controllers. A coleção Postman antiga em `src/main/resources/collections/` pertence à versão acadêmica e não representa o contrato atual.

### Exemplo de pedido

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

O servidor consulta o preço vigente, valida os itens e devolve `restauranteId`, `itens` (com `nomePrato`, `precoUnitario` e `subtotal`), `valorTotal`, status e datas. O cliente não define preço, total ou status. O antigo corpo com `pratos` foi substituído por `itens`.

Erros usam `status`, `error`, `message`, `path` e `timestamp`. Em geral: `400` para entrada malformada, `404` para recurso ausente, `409` para conflito de regra e `422` para campos inválidos ou estimativa sem coordenadas.

## Estrutura do repositório

```text
src/main/java/.../
  controller/       rotas HTTP
  dto/              entrada de criação de pedido
  entity/           modelo persistido
  exception/        erros padronizados
  repository/       acesso a dados
  service/          regras e integrações externas
src/main/resources/static/   interface e catálogo de demonstração
src/test/                    testes de integração
.github/workflows/           CI e publicação no Pages
Dockerfile                   backend opcional em container
```

## Publicar no GitHub Pages

O workflow [`pages.yml`](.github/workflows/pages.yml) publica os arquivos de `src/main/resources/static/` em cada push na `main` que altera a interface.

1. Faça commit e push das mudanças.
2. No GitHub, abra **Settings → Pages**.
3. Em **Build and deployment → Source**, selecione **GitHub Actions**.
4. Abra a aba **Actions** e confirme que **Publicar demonstração** terminou sem erro.
5. Acesse `https://thgcunha.github.io/SistemaDelivery/` e teste catálogo, sacola, pedido e status.

O GitHub Pages serve apenas arquivos estáticos; não executa Spring Boot. O modo de demonstração é funcional no navegador, mas não compartilha pedidos entre visitantes. Para ligar o Pages a um backend hospedado, abra a URL com `?api=https://SEU-BACKEND.onrender.com`. Nesse caso configure `APP_CORS_ALLOWED_ORIGINS=https://thgcunha.github.io` no backend. Consulte a [documentação do GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site).

## Backend hospedado (opcional)

O [`Dockerfile`](Dockerfile) prepara a API para um Web Service Docker, por exemplo no Render. Se optar por isso, conecte o repositório, escolha **Docker** e publique a pasta raiz. O aplicativo usa a porta definida por `PORT`. O H2 atual é **volátil**: reinicializações apagam pedidos e recarregam o catálogo. Para dados duráveis, é necessário configurar um banco persistente e migrações antes de tratar o serviço como produção. Serviços gratuitos também podem hibernar e demorar a responder. O Pages funciona independentemente dessa hospedagem.

## Limites da demonstração

- Restaurantes e pratos da vitrine são fictícios; os valores são de demonstração.
- Endereços e rotas dependem de APIs públicas; quando falham, o pedido pode continuar sem estimativa.
- Não há autenticação, pagamento, cobrança de frete ou persistência durável na versão pública.
- O projeto demonstra arquitetura, regras e fluxo de interface; não é um serviço comercial de delivery.

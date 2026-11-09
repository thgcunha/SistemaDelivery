# Sistema Delivery

API REST para gerenciamento de restaurantes, pratos e pedidos, desenvolvida originalmente na disciplina de Programação Orientada a Objetos II. O projeto está sendo refatorado em etapas para compor um portfólio de desenvolvimento Java.

## Estado atual

Esta primeira etapa corrige a configuração de compilação e a execução do Maven Wrapper no Windows. A aplicação ainda passa por revisão das validações, dos relacionamentos e das regras de pedidos. Ainda não há interface web nem demonstração publicada.

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
- Criação de pedidos com cálculo do total a partir dos preços cadastrados.
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

O comando `clean verify` compila, executa os testes e gera o JAR em `target/`. Atualmente há um teste de inicialização do contexto Spring; os testes das regras de negócio serão adicionados junto às respectivas correções.

Depois do build, também é possível executar o JAR:

```sh
java -jar target/universidadeESN3-0.0.1-SNAPSHOT.jar
```

## Próximas etapas

1. Corrigir respostas de erro e validações, com testes de regressão.
2. Corrigir relacionamentos, cálculo de valores e transições de pedidos.
3. Evoluir os contratos da API, a persistência e as dependências.
4. Integrar consulta de endereço por CEP.
5. Criar a interface web e o fluxo completo de pedido.
6. Adicionar rota estimada e cálculo de frete.
7. Documentar a API e publicar a demonstração.

Integração com pagamentos está fora do escopo desta evolução. A coleção Postman em `src/main/resources/collections/` é um artefato do projeto acadêmico anterior e ainda não documenta as rotas atuais.

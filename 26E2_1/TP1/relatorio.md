# Relatório do Projeto: Serviço Web REST de Operações Matemáticas

## Escolha da Ferramenta de Construção
Para este projeto, foi escolhido o **Maven**. O Maven foi adotado por ter alta popularidade, possuir um repositório centralizado vasto para gerenciamento de dependências, além de possuir configuração declarativa baseada no `pom.xml`, o que o torna altamente padronizado para projetos Spring Boot. Já o Gradle tem vantagens em performance (graças ao seu daemon e build incremental), porém a sintaxe estruturada do Maven garante alta legibilidade e previsibilidade para este ciclo inicial de estudo em Spring Boot.

## Iniciação do Projeto
Existem duas formas principais muito produtivas para inciar o projeto:
1. **Spring Initializr via Web (start.spring.io):** Possibilita selecionar visualmente as versões do Spring, a linguagem Java, e as dependências (como Web). Ideal quando se pretende obter uma visão abrangente e configurável do esqueleto do projeto através de uma interface intuitiva.
2. **Spring Boot CLI:** Útil quando se quer uma inicialização rápida e direta pela linha de comando. Recomenda-se para desenvolvedores experientes que buscam agilidade na criação local do projeto. (Exemplo de comando de iniciação `spring init --dependencies=web myproject`).

## Gerenciamento de Dependências
As dependências do Spring são gerenciadas pelo pai (parent POM) `spring-boot-starter-parent` que centraliza e previne o conflito de versões entre diferentes bibliotecas, assim não precisamos especificar versões para todas as dependências geridas. No projeto incluímos o empacotamento base `spring-boot-starter-web` para a API REST que já embuti o Tomcat e demais ferramentas necessárias, simplificando radicalmente o desenvolvimento e a manutenção.

## Utilização de Autoconfiguração
Através da anotação `@SpringBootApplication`, ativamos o recurso de autoconfiguração inteligente (`@EnableAutoConfiguration`) do Spring Boot na classe `MathApplication`. Isto elimina a necessidade de criar configurações explícitas para o DispatcherServlet do web, além da configuração do servidor web Tomcat interno; o Spring levanta os componentes automaticamente baseado no `classpath`.

## Configuração da IDE
O IntelliJ IDEA é fortemente aconselhado. Para abri-lo:
1. Navegue até File -> Open e selecione a pasta `TP1` que carrega o `pom.xml`.
2. A IDE reconhecerá automaticamente a estrutura como projeto Maven.
3. Configure o SDK para Java 21 em `File -> Project Structure`.
*(Aqui em um relatório real devem ser inseridas capturas de tela dos passos descritos).*

## Desenvolvimento de Serviços REST
Foi criado um `@RestController` denominado `MathController`, localizado em `org.example.controller`. Neste controlador criamos os seguintes métodos e mapeamentos (com `@RequestMapping` suportando tanto `GET` e `POST`):
- Adição: `/math/add?a=x&b=y`
- Subtração: `/math/subtract?a=x&b=y`
- Multiplicação: `/math/multiply?a=x&b=y`
- Divisão: `/math/divide?a=x&b=y`
- Exponenciação: `/math/power?a=x&b=y`

Cada endpoint recebe dois parâmetros `a` e `b`. A abordagem utiliza anotações padrão do Spring de forma limpa, garantindo a natureza sem estado (stateless) para lidar com requisições. O suporte multitarefa GET/POST provém da anotação de atributos method: `method = {RequestMethod.GET, RequestMethod.POST}`.

## Testes e Exemplos
Se acessarmos a rota `/math/add?a=10&b=5` via requisição web (método GET), a resposta obtida será `15.0`. Já se houver submissão de um form body (método POST) nos mesmos parâmetros, também haverá idêntico comportamento, garantindo robustez para diversos fluxos de clientes.


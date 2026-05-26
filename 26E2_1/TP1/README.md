# 🧮 API Matemática REST - TP1

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Maven-3.8+-blue.svg" alt="Maven">
  <img src="https://img.shields.io/badge/Instituição-Infnet-red.svg" alt="Instituto Infnet">
</p>

Este projeto é a entrega da avaliação **TP1** da disciplina de **Desenvolvimento de Serviços com Spring Boot** no **Instituto Infnet**.

Consiste em um serviço web REST *stateless* (sem estado) para a realização de operações matemáticas básicas.

---

## 📄 Relatório do Projeto

Este README contém as orientações essenciais para rodar o projeto. Para ler as documentações acadêmicas exigidas, como a justificativa das escolhas tecnológicas (Maven vs Gradle, inicialização, etc) e capturas de tela, por favor, **[consulte o nosso relatorio.md completo aqui](relatorio.md)**.

---

## 🚀 Funcionalidades

A API fornece 5 (cinco) endpoints correspondentes às operações matemáticas elementares. Uma das exigências da construção desta API é o suporte para requisições do tipo **GET** e **POST** no mesmo mapeamento, ambas entregando exatamente o mesmo comportamento estatístico.

- ➕ **Adição**
- ➖ **Subtração**
- ✖️ **Multiplicação**
- ➗ **Divisão**
- 📈 **Exponenciação**

## 🛠️ Tecnologias Utilizadas

* **Java 21** - Linguagem de programação
* **Spring Boot 3.2.5** - Framework principal
* **Spring Web** - Para a construção dos serviços REST e injeção do Apache Tomcat servidor
* **Maven** - Gerenciador de dependências e automação de builds

---

## ⚙️ Como Executar o Projeto

**Pré-requisitos**: Ter o [Java JDK 21](https://jdk.java.net/21/) e o [Apache Maven](https://maven.apache.org/) instalados na sua máquina.

1. Clone o repositório ou baixe a pasta.
2. Navegue até a raiz do projeto (onde está o arquivo `pom.xml`).
3. Abra o seu terminal e execute o comando:
   ```bash
   mvn spring-boot:run
   ```
4. A aplicação estará ativa em: `http://localhost:8080`

*(Alternativamente, você pode importar a pasta como um projeto Maven no seu **IntelliJ IDEA** e iniciar a classe `MathApplication.java`)*

---

## 📌 Utilizando os Endpoints (Parâmetros)

URL Base: `http://localhost:8080/math`

Os parâmetros de entrada nas requisições são sempre `a` e `b`. 

| Operação | Caminho | Parâmetros (URL Params ou Form Body) | Exemplo de Requisição (GET) | Resposta Esperada |
| :--- | :--- | :--- | :--- | :--- |
| **Adição** | `/add` | `a` (double), `b` (double) | `/math/add?a=10&b=2` | `12.0` |
| **Subtração** | `/subtract` | `a` (double), `b` (double) | `/math/subtract?a=10&b=2` | `8.0` |
| **Multiplicação** | `/multiply` | `a` (double), `b` (double) | `/math/multiply?a=10&b=2` | `20.0` |
| **Divisão** | `/divide` | `a` (double), `b` (double) | `/math/divide?a=10&b=2` | `5.0` |
| **Exponenciação** | `/power` | `a` (double), `b` (double) | `/math/power?a=10&b=2` | `100.0` |

### Exemplo via cURL (Validando o POST)
```bash
curl -X POST -F "a=10" -F "b=5" http://localhost:8080/math/multiply
# Retorna: 50.0
```

---
Feito com dedicação para a disciplina de **Desenvolvimento de Serviços com Spring Boot** 🎓


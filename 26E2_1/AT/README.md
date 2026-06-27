# Hospital API

Este projeto consiste em uma API REST desenvolvida para a avaliação trimestral (AT) da disciplina de Desenvolvimento de Serviços com Spring Boot [26E2_1] do Instituto Infnet.

A aplicação implementa o gerenciamento de uma clínica/hospital, contendo o cadastro de pacientes, médicos e o agendamento de consultas médicas.

---

## Tecnologias e Dependências

O projeto foi desenvolvido utilizando as seguintes tecnologias:
- Java 21
- Spring Boot 3.3.1
- Spring Data JPA (com Hibernate)
- Spring Boot Validation (validação de DTOs)
- Spring Boot Actuator (healthcheck e monitoramento)
- PostgreSQL 15 (banco de dados principal)
- H2 Database (banco em memória para testes)
- Docker e Docker Compose (containerização da aplicação e banco)
- JUnit 5 e MockMvc (testes de integração e unidade)

---

## Modelagem e Relacionamentos

As entidades principais e seus relacionamentos JPA são:
- **Paciente (Paciente):** Relacionamento de um-para-muitos (OneToMany) com Consulta e com Internacao. Campo CPF configurado como único.
- **Médico (Medico):** Relacionamento de um-para-muitos (OneToMany) com Consulta.
- **Consulta (Consulta):** Vincula Paciente e Médico. Registra a data/hora e observações da consulta.
- **Internação (Internacao):** Relacionamento de muitos-para-um (ManyToOne) com Paciente, armazenando quarto, data de entrada e de alta.

---

## Estrutura do Código

O projeto está dividido nos seguintes pacotes:
- `config`: Configuração do seed de dados inicial.
- `controller`: Controllers REST expondo os endpoints.
- `dto`: Classes de transferência de dados (Request e Response DTOs) para evitar o acoplamento direto com as entidades JPA.
- `exception`: Tratamento de exceções com `@ControllerAdvice` e exceções personalizadas de negócio.
- `model`: Entidades JPA de mapeamento de banco de dados.
- `repository`: Interfaces do Spring Data JPA.
- `service`: Camada de lógica de negócio e regras da aplicação.

---

## Perfis de Configuração (Profiles)

O comportamento do banco de dados e da carga de dados varia conforme o profile ativo:

1. **Profile default:**
   - Banco de dados: PostgreSQL (porta 5432).
   - Executa a carga inicial de dados mock de médicos e pacientes (`CommandLineRunnerConfig`).
   - Arquivo de configuração: `src/main/resources/application.yml`.

2. **Profile test:**
   - Banco de dados: H2 em memória.
   - A carga inicial de dados mock é desativada para manter a consistência e isolamento dos testes automatizados.
   - Arquivo de configuração: `src/test/resources/application-test.yml`.

---

## Como Executar a Aplicação

### Opção 1: Usando Docker Compose (Recomendado)
Para subir o banco de dados e a aplicação compilada em containers:
```bash
docker-compose up --build -d
```
A API ficará disponível no endereço: `http://localhost:8080`

Para acompanhar os logs da aplicação:
```bash
docker-compose logs -f app
```

Para encerrar a execução:
```bash
docker-compose down
```

### Opção 2: Execução Local
1. Suba apenas o container do banco de dados:
   ```bash
   docker-compose up db -d
   ```
2. Execute o projeto usando o Maven ou diretamente pela IDE (classe `HospitalApiApplication`):
   ```bash
   mvn spring-boot:run
   ```

---

## Execução dos Testes

Os testes unitários e de integração podem ser executados com o comando:
```bash
mvn test
```
Os testes utilizam automaticamente o profile de `test` com banco H2 em memória.

---

## Endpoints Disponíveis

### Pacientes (`/api/pacientes`)

- **Cadastrar Paciente**
  - Método: `POST`
  - URL: `/api/pacientes`
  - Body (JSON):
    ```json
    {
      "nome": "Lucas Lima",
      "cpf": "222.333.444-55",
      "dataNascimento": "1995-08-25",
      "telefone": "(31) 98888-7777"
    }
    ```
  - Retorno (201 Created):
    ```json
    {
      "id": 3,
      "nome": "Lucas Lima",
      "cpf": "222.333.444-55",
      "dataNascimento": "1995-08-25",
      "telefone": "(31) 98888-7777"
    }
    ```

- **Buscar Paciente por ID**
  - Método: `GET`
  - URL: `/api/pacientes/{id}`
  - Retorno (200 OK)

- **Listar Todos os Pacientes**
  - Método: `GET`
  - URL: `/api/pacientes`
  - Retorno (200 OK)

- **Remover Paciente**
  - Método: `DELETE`
  - URL: `/api/pacientes/{id}`
  - Retorno (204 No Content)

---

### Médicos (`/api/medicos`)

- **Cadastrar Médico**
  - Método: `POST`
  - URL: `/api/medicos`
  - Body (JSON):
    ```json
    {
      "nome": "Dra. Ana Paula",
      "crm": "CRM-54321",
      "especialidade": "Pediatra"
    }
    ```
  - Retorno (201 Created)

- **Listar Todos os Médicos**
  - Método: `GET`
  - URL: `/api/medicos`
  - Retorno (200 OK)

- **Ranking de Médicos (Ordenado por número de consultas)**
  - Método: `GET`
  - URL: `/api/medicos/ranking`
  - Retorno (200 OK): Retorna a lista de médicos cadastrados, ordenados de forma decrescente pela quantidade de consultas associadas.

---

### Consultas (`/api/consultas`)

- **Agendar Consulta**
  - Método: `POST`
  - URL: `/api/consultas`
  - Body (JSON):
    ```json
    {
      "dataConsulta": "2026-07-10T14:30:00",
      "observacoes": "Consulta de retorno anual de cardiologia.",
      "pacienteId": 1,
      "medicoId": 1
    }
    ```
  - Retorno (201 Created)

---

### Monitoramento (`/actuator`)

- **Healthcheck**
  - Método: `GET`
  - URL: `/actuator/health`
  - Retorno:
    ```json
    {
      "status": "UP"
    }
    ```

---

## Tratamento de Erros

A aplicação utiliza um manipulador global para padronizar os erros:
- **Erro de Validação (400 Bad Request):** Retornado ao tentar enviar dados inválidos (como nome ou CPF em branco). Contém a lista de erros de validação correspondentes.
- **Recurso Não Encontrado (404 Not Found):** Retornado quando um ID solicitado (Paciente ou Médico) não é localizado no banco de dados.

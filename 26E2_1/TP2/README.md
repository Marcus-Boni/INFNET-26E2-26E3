# Sistema de Reserva de Passagens de Ônibus (TP2)

Este repositório contém a implementação do TP2, estruturado estritamente sem comentários nos arquivos de código-fonte. Todas as explicações e a documentação do projeto estão reunidas neste arquivo de forma simples, direta e eficiente.

---

## 📂 Estrutura do Repositório

O diretório é dividido em dois projetos Spring Boot independentes, gerados com suporte a **Java 21**, **Spring Web**, **DevTools** e **Lombok**:
1. **`principal/`**: Projeto que contém a implementação completa do CRUD em memória e das regras de negócio.
2. **`secundario/`**: Projeto complementar gerado a partir do terminal para fixação de comandos da API do Spring Initializr.

---

## 🏗️ Camadas do Projeto Principal

No pacote `br.edu.infnet.principal`, o código é estruturado de forma desacoplada em quatro camadas limpas:

### 1. `models`
- **`Passagem`**: Classe de domínio que representa o bilhete de ônibus com os campos `id`, `passageiro`, `assento`, `origem`, `destino`, `data` (LocalDate) e `status`. Utiliza as anotações do Lombok `@Data`, `@NoArgsConstructor` e `@AllArgsConstructor`.

### 2. `dtos`
- **`PassagemRequestDTO`**: Objeto para recebimento de dados na criação/atualização de passagens (sem o campo `id` e sem validações de anotação).
- **`PassagemResponseDTO`**: Objeto para retorno de dados contendo o `id` e todos os campos preenchidos.

### 3. `services`
- **`PassagemService`**: Gerencia a lógica de negócio e mantém a lista em memória:
  - `List<Passagem> passagens = new ArrayList<>()`
  - `Long idCounter = 1L`
  - **Carga Inicial**: O construtor semeia a lista com 3 passagens padrão, incrementando o contador.
  - **Conversões**: Métodos `convertToEntity` e `convertToDTO`.
  - **POST**: Valida se o assento já existe na lista. Se sim, lança `400 Bad Request` (`ResponseStatusException`).
  - **GET {id}**: Busca por ID. Se não encontrado, lança `404 Not Found`.
  - **PUT {id}**: Atualiza os campos se o ID existir e valida se o novo assento não conflita com outra reserva. Se não existir o ID, lança `404 Not Found`.
  - **DELETE {id}**: Remove pelo ID usando `removeIf`. Se nenhum registro for removido, lança `404 Not Found`. Retorna `204 No Content`.
  - **Busca**: Filtra e retorna passagens por destino de forma case-insensitive (`equalsIgnoreCase`).

### 4. `controllers`
- **`PassagemController`**: Expõe os endpoints REST mapeados em `/passagens`:
  - `GET /passagens`: Lista todas as passagens (retorna `200 OK`).
  - `GET /passagens/{id}`: Busca uma passagem específica por ID.
  - `POST /passagens`: Cria uma nova passagem (retorna `201 Created`).
  - `PUT /passagens/{id}`: Atualiza uma passagem existente por ID.
  - `DELETE /passagens/{id}`: Deleta uma passagem por ID (retorna `204 No Content`).
  - `GET /passagens/busca?destino=X`: Busca passagens pelo destino.

---

## 🔬 Validação Automatizada ( verify_api.ps1 )

Para testar de forma ágil todas as regras sem a necessidade de ferramentas visuais externas, desenvolvemos o script PowerShell `verify_api.ps1`. O script executa as seguintes validações contra a API ativa:

1. **Listagem Inicial**: Valida se a API inicia com exatamente 3 passagens semeadas.
2. **Criação com Sucesso**: Adiciona uma nova passagem com assento `20` e valida se o status retornado é `201 Created`.
3. **Bloqueio de Assento Duplicado**: Tenta cadastrar outra passagem com o mesmo assento `20` e valida o erro `400 Bad Request`.
4. **Busca por ID**: Valida se a busca por ID existente retorna `200 OK` e dados corretos, e se IDs inexistentes retornam `404 Not Found`.
5. **Atualização**: Modifica os dados da passagem de ID `1` e verifica o salvamento. A tentativa de atualizar um ID inválido retorna `404 Not Found`.
6. **Filtro de Destino**: Busca por `?destino=belo horizonte` (em minúsculas) e valida se encontra os registros ignorando a capitalização.
7. **Deleção**: Deleta a passagem de ID `1` (valida status `204 No Content`), verifica se o subsequente GET por esse ID retorna `404 Not Found` e valida que a tentativa de excluir um ID inexistente lança `404 Not Found`.

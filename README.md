# HORIZONTEZS — Gestão de Comércio Exterior

Aplicação web acadêmica para consultar e administrar cadastros e operações de comércio exterior. A interface usa Thymeleaf e Bootstrap, com autenticação, controle de acesso por perfis e persistência em H2.

## Funcionalidades

- Visão geral com contagem de operações, empresas, parceiros, transportadoras, produtos, taxas de câmbio e indicadores.
- Cadastro e consulta de operações, empresas, parceiros, produtos, transportadoras, taxas de câmbio e indicadores mensais.
- Importação opcional dos dados de uma planilha `.xlsx`.
- Login com usuários persistidos, senha criptografada com BCrypt, gestão de sessão e troca de senha.
- Perfis `USER` (consulta) e `ADMIN` (administração e alterações), com permissões verificadas no backend.
- Área administrativa para gerenciar usuários, perfis e permissões.

## Tecnologias

- Java 21
- Spring Boot 4.1.2, Spring MVC e Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf e Bootstrap 5.3
- H2 Database
- Apache POI para leitura de arquivos Excel
- Maven Wrapper

## Pré-requisitos

- JDK 21 instalado e configurado no `PATH`.
- PowerShell no Windows (ou IntelliJ IDEA configurado para usar o JDK 21).
- Acesso à internet na primeira compilação para baixar as dependências Maven.

## Configurar as contas iniciais

Antes de iniciar, configure **todas as oito variáveis** abaixo. Elas são exigidas pelo `application.properties` mesmo em inicializações seguintes. As contas são provisionadas de forma idempotente: se já existirem, o sistema não cria duplicatas nem substitui suas senhas.

No PowerShell, defina os dados de usuário e administrador. Substitua os exemplos de nome e e-mail pelos valores desejados e informe senhas próprias:

```powershell
$env:APP_BOOTSTRAP_USER_USERNAME = "usuario"
$env:APP_BOOTSTRAP_USER_NAME = "Usuário de consulta"
$env:APP_BOOTSTRAP_USER_EMAIL = "usuario@exemplo.com"
$env:APP_BOOTSTRAP_USER_PASSWORD = "<defina-uma-senha-forte>"

$env:APP_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:APP_BOOTSTRAP_ADMIN_NAME = "Administrador"
$env:APP_BOOTSTRAP_ADMIN_EMAIL = "admin@exemplo.com"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = "<defina-outra-senha-forte>"
```

As senhas precisam ter de 10 a 72 bytes em UTF-8, com letra maiúscula, minúscula, número e símbolo. As senhas são gravadas no banco somente como hashes BCrypt. Não coloque senhas reais neste README, no código ou em arquivos versionados.

As variáveis definidas no PowerShell valem para aquela sessão e são herdadas pelo processo iniciado nela. No IntelliJ IDEA, configure as mesmas oito variáveis em **Run → Edit Configurations… → Environment variables** para a configuração `ComercioExterirorApplication`.

## Executar no Windows

Na raiz do projeto, defina as variáveis de ambiente acima e inicie a aplicação:

```powershell
.\mvnw.cmd spring-boot:run
```

Ou, no IntelliJ IDEA:

1. Abra o projeto e aguarde o Maven carregar as dependências.
2. Configure o Project SDK como Java 21.
3. Na configuração de execução `ComercioExterirorApplication`, configure as oito variáveis de ambiente.
4. Execute a classe `ComercioExterirorApplication`.

Quando o log indicar que o servidor iniciou, acesse [http://localhost:8080](http://localhost:8080). A aplicação encaminha visitantes sem sessão para a tela de login. Após o primeiro login, as contas provisionadas inicialmente devem trocar a senha.

Para executar os testes automatizados:

```powershell
.\mvnw.cmd test
```

## Banco de dados

O banco H2 é criado em `./data/comercioexterior` relativo ao diretório de trabalho da aplicação. O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=update`, para atualizar o esquema sem recriar o banco. Preserve essa pasta para manter os dados locais.

A console do H2 fica em [http://localhost:8080/h2-console](http://localhost:8080/h2-console) e é restrita ao perfil `ADMIN`. A configuração JDBC padrão é `jdbc:h2:file:./data/comercioexterior`; usuário `sa`, sem senha.

## Acessos e permissões

| Endereço | Acesso |
| --- | --- |
| `/login` | Login de usuários cadastrados |
| `/` | Visão geral e consulta; exige autenticação |
| `/operacoes`, `/empresas`, `/parceiros`, `/produtos`, `/transportadoras`, `/taxas-cambio`, `/indicadores` | Consulta para `USER` e `ADMIN`; alterações para `ADMIN` |
| `/admin` | Painel administrativo; exclusivo de `ADMIN` |
| `/admin/usuarios`, `/admin/perfis`, `/admin/permissoes` | Gestão administrativa; exclusivo de `ADMIN` |
| `/conta/senha` | Alteração da própria senha por usuário autenticado |

O logout encerra a sessão e é feito por formulário protegido por CSRF.

## Importação opcional de planilha

A importação automática fica desabilitada no uso normal (`app.importacao.planilha.executar=false`). Para executá-la intencionalmente pelo IntelliJ, adicione estes argumentos de programa à configuração da aplicação, substituindo o caminho pelo arquivo `.xlsx`:

```text
--app.importacao.planilha.executar=true --app.importacao.planilha.caminho="C:\dados\planilha.xlsx"
```

A importação usa as abas e colunas reconhecidas pelo importador do projeto. Remova os argumentos para iniciar o site sem importar a planilha.

## Estrutura do projeto

```text
src/main/java/.../config       Configurações Spring e segurança
src/main/java/.../controller   Controllers MVC
src/main/java/.../dto          Objetos de formulário e transferência
src/main/java/.../model        Entidades JPA
src/main/java/.../repository   Repositórios Spring Data
src/main/java/.../security     Autenticação e regras de autorização
src/main/java/.../service      Regras de negócio e importação
src/main/resources/templates   Páginas Thymeleaf
src/main/resources/static      CSS, JavaScript, imagens e vídeos
src/test                       Testes automatizados
```

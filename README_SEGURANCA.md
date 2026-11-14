# Autenticação e controle de acesso

O sistema usa autenticação por formulário com usuários persistidos no banco H2. As senhas são armazenadas como hashes BCrypt. A autorização usa perfis (`ADMIN` e `USER`) e permissões associadas aos perfis.

## Primeiro início

Antes de iniciar a aplicação em um banco sem usuários, configure uma senha para cada conta inicial. O usuário visualizador aceita username, nome e e-mail padrão (`usuario`, `Usuario padrao` e `usuario@local.invalid`); a senha não tem valor padrão. O administrador precisa ter username, e-mail e senha definidos. As duas senhas precisam atender à política: 10 a 72 bytes UTF-8, com letra maiúscula, minúscula, número e símbolo.

No PowerShell, substitua os valores entre aspas pelos dados que deseja usar:

```powershell
$env:APP_BOOTSTRAP_USER_PASSWORD = "<senha forte do usuário>"
$env:APP_BOOTSTRAP_ADMIN_USERNAME = "<username administrador>"
$env:APP_BOOTSTRAP_ADMIN_NAME = "<nome do administrador>"
$env:APP_BOOTSTRAP_ADMIN_EMAIL = "<e-mail do administrador>"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = "<senha forte do administrador>"
.\mvnw.cmd spring-boot:run
```

Opcionalmente, personalize os dados iniciais do usuário de consulta com `APP_BOOTSTRAP_USER_USERNAME`, `APP_BOOTSTRAP_USER_NAME` e `APP_BOOTSTRAP_USER_EMAIL`. A senha do usuário pode ser trocada pela variável `APP_BOOTSTRAP_USER_PASSWORD`.

O provisionamento é idempotente: executa apenas para contas que ainda não existem e não substitui senhas ou dados já cadastrados. Portanto, remova as variáveis da sessão após o início, se preferir:

```powershell
Remove-Item Env:APP_BOOTSTRAP_USER_PASSWORD
Remove-Item Env:APP_BOOTSTRAP_ADMIN_PASSWORD
```

## Acesso e permissões

- `/login`: autenticação; usuários provisionados inicialmente precisam trocar a senha no primeiro acesso.
- `/`: painel e consulta geral, disponíveis a `USER` e `ADMIN`.
- `/admin/**`: área administrativa exclusiva de `ADMIN`.
- Cadastros: `USER` pode consultar; `ADMIN` pode consultar e alterar. O bloqueio de escrita é aplicado no backend e nas operações de serviço.
- `/conta/senha`: alteração da própria senha para usuário autenticado.
- `/logout`: encerra a sessão por POST e exige o token CSRF do formulário.
- `/acesso-negado`: página de acesso negado (403). A aplicação MVC redireciona usuários anônimos para `/login`.

O banco H2 existente é preservado. Como o projeto não tinha Flyway nem Liquibase, o Hibernate com `spring.jpa.hibernate.ddl-auto=update` acrescenta as tabelas de usuários, perfis e permissões e suas tabelas de associação. A console H2 permanece restrita a `ADMIN`.

Após autenticar como administrador, cadastre e gerencie usuários, perfis e permissões pelo menu Administração. Para conceder acesso somente de leitura, associe o perfil `USER`; para operações de escrita e administração, use `ADMIN`.

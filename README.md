# MTM (Multi-Tenancy Manager) - Core Reactor

O **MTM Core Reactor** é uma biblioteca projetada para facilitar a implementação de arquiteturas multi-tenant (banco de dados por tenant) em aplicações **Spring WebFlux** utilizando **Kotlin**.

Este módulo abstrai a complexidade de roteamento dinâmico de conexões de banco de dados (R2DBC para SQL e Reactive Mongo para NoSQL), permitindo que você transforme qualquer projeto Spring Boot reativo em uma aplicação multi-tenant robusta, desde que siga os requisitos de compatibilidade.

## 🚀 Funcionalidades

*   **Segregação por Banco de Dados:** Cada tenant possui seu próprio banco de dados, garantindo isolamento total dos dados.
*   **Suporte a Spring WebFlux:** Totalmente integrado ao ecossistema reativo do Spring (Project Reactor).
*   **Roteamento Dinâmico:** Criação e cache de pools de conexão sob demanda baseados no `TenantContext`.
*   **Suporte a Múltiplos Bancos:**
    *   PostgreSQL (R2DBC)
    *   MySQL (R2DBC)
    *   MongoDB (Reactive Streams)
*   **Configuração Flexível:** Definição de pacotes de repositórios para dados globais, compartilhados e específicos de tenant.

## 📋 Compatibilidade

Para utilizar esta biblioteca, seu projeto deve atender aos seguintes requisitos mínimos:

| Tecnologia | Versão Mínima / Recomendada |
| :--- | :--- |
| **Java** | 21 |
| **Kotlin** | 2.2.21 |
| **Spring Boot** | 4.0.0 (Reactive Stack) |

## 📦 Instalação

Adicione a dependência ao seu `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.iggdrasil:mtm-core-reactor:0.0.1-rc17-SNAPSHOT")
}
```

Certifique-se de que seu projeto está configurado para usar os repositórios corretos (ex: GitHub Packages ou Maven Central, conforme a distribuição).

## ⚙️ Configuração

A configuração é feita inteiramente via `application.yml` (ou `.properties`). Você deve definir as credenciais do banco de dados base e os pacotes onde seus repositórios estão localizados.

### Exemplo de `application.yml`

```yaml
mtm:
  data-source:
    type: POSTGRES # ou MYSQL, MONGO
    host: localhost
    port: 5432
    username: meu_usuario
    password: minha_senha
    database: base_db # Nome base para os bancos (ex: base_db_tenant1)
    max-pool-size: 10
  
  repositories:
    # Repositórios que acessam o banco 'global' (sem contexto de tenant)
    main:
      - "com.exemplo.app.repository.main"
    
    # Repositórios que acessam dados compartilhados (se houver)
    shared:
      - "com.exemplo.app.repository.shared"
    
    # Repositórios que acessam dados específicos do tenant (Roteamento Dinâmico)
    tenant:
      - "com.exemplo.app.repository.tenant"
```

### Detalhes das Propriedades

*   `mtm.data-source.type`: Tipo do banco de dados (`POSTGRES`, `MYSQL`, `MONGO`).
*   `mtm.data-source.database`: O prefixo ou nome base do banco de dados. O sistema irá procurar/criar conexões para `base_db` (global) ou `base_db-<tenant_id>` (tenant).
*   `mtm.repositories.*`: Lista de pacotes onde o Spring Data deve procurar por interfaces de repositório. É **crucial** separar seus repositórios nesses pacotes para que a injeção de dependência funcione corretamente com o contexto certo.

## 🛠️ Como Usar

### 1. Contexto do Tenant (`TenantContext`)

A biblioteca utiliza o `Reactor Context` para propagar o ID do tenant através da cadeia de execução reativa.

Para definir o tenant atual (ex: em um filtro de segurança ou interceptor):

```kotlin
import io.iggdrasil.mtm.tenant.TenantContext

// ... dentro de um fluxo reativo
chain.filter(exchange)
    .contextWrite { ctx -> TenantContext.write(ctx, "tenant-123") }
```

Para ler o tenant atual (caso precise manualmente):

```kotlin
TenantContext.read()
    .flatMap { tenantId -> 
        // Lógica com o tenantId
    }
```

### 2. Estrutura de Pastas Sugerida

Organize seu código para separar claramente o que é global do que é específico de tenant:

```text
src/main/kotlin/com/exemplo/app
├── repository
│   ├── main      // Tabelas de usuários globais, configurações do sistema
│   │   └── UserRepository.kt
│   └── tenant    // Tabelas de produtos, pedidos (específicos de cada cliente)
│       └── ProductRepository.kt
└── service
    └── ...
```

### 3. Injeção de Dependência

Basta injetar seus repositórios normalmente. O MTM cuidará de fornecer a conexão correta baseada no pacote do repositório e no contexto atual.

```kotlin
@Service
class ProductService(private val productRepository: ProductRepository) {
    
    fun findAll() = productRepository.findAll() // Automaticamente busca no banco do tenant atual
}
```

## 🏗️ Arquitetura Interna

O núcleo do funcionamento reside no `TenantAwareConnectionFactory`. Ele intercepta a criação de conexões R2DBC:

1.  Verifica se há um `tenant-id` no Contexto Reativo.
2.  Se houver, busca (ou cria) um pool de conexões específico para aquele tenant (ex: conectando em `db_tenant_123`).
3.  Se não houver, utiliza o pool global (ex: conectando em `db_global`).
4.  Os pools são cacheados e descartados após um período de inatividade para economizar recursos.

---
**Desenvolvido por Iggdrasil IO**

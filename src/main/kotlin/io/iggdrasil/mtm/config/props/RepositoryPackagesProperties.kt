package io.iggdrasil.mtm.config.props

/**
 * Pacotes dos repositórios Spring Data de cada escopo (`mtm.repositories.*`).
 *
 * - [main]: banco GLOBAL (`mtm.data-source.database`), sempre — com ou sem tenant no contexto.
 * - [tenant]: banco do tenant do contexto (`<database>-<tenant>`); sem tenant, o global.
 */
data class RepositoryPackagesProperties(
    var main: List<String> = emptyList(),
    var tenant: List<String> = emptyList(),
)

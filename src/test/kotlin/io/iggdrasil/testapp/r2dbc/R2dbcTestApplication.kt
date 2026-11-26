package io.iggdrasil.testapp.r2dbc

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * App de teste num pacote FORA do da lib (como um projeto consumidor): o que a lib faz
 * tem de chegar só pela auto-configuração, sem component scan do pacote dela.
 */
@SpringBootApplication
class R2dbcTestApplication

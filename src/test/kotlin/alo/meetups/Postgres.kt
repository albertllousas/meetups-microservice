package alo.meetups

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer

class Postgres {

    val container: KtPostgreSQLContainer = KtPostgreSQLContainer()
        .withNetwork(Network.newNetwork())
        .withNetworkAliases("localhost")
        .withUsername("meetups")
        .withPassword("meetups")
        .withDatabaseName("meetups")
        .also {
            it.start()
            Flyway(FluentConfiguration().dataSource(it.jdbcUrl, it.username, it.password)).migrate()
        }
}

class KtPostgreSQLContainer : PostgreSQLContainer<KtPostgreSQLContainer>("postgres:latest")

package alo.meetups.infrastructure.config

import io.agroal.api.AgroalDataSource
import io.quarkus.runtime.Startup

@Startup
class StartupChecks(dataSource: AgroalDataSource) {

    val checkDatabase = { dataSource.connection }

    init {
        checkDatabase()
    }
}
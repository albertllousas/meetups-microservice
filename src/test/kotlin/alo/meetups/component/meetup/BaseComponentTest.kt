package alo.meetups.component.meetup

import alo.meetups.Postgres
import alo.meetups.fixtures.stubHttpEndpointForFindUserSucceeded
import com.github.tomakehurst.wiremock.WireMockServer
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector.MatchesType
import org.junit.jupiter.api.BeforeEach

class Resources : QuarkusTestResourceLifecycleManager {

    private lateinit var db: Postgres

    private lateinit var server: WireMockServer

    override fun start(): MutableMap<String, String> {
        db = Postgres()
        server = WireMockServer().also { it.start() }
        return mutableMapOf(
            "quarkus.datasource.jdbc.url" to db.container.jdbcUrl,
            "clients.users-service.url" to server.baseUrl()
        )
    }

    @Synchronized
    override fun stop() {
        db.container.stop()
        server.stop()
    }

    override fun inject(testInjector: TestInjector) {
        testInjector.injectIntoFields(db, MatchesType(Postgres::class.java))
        testInjector.injectIntoFields(server, MatchesType(WireMockServer::class.java))
    }
}

@QuarkusTestResource(Resources::class)
abstract class BaseComponentTest {

    protected lateinit var db: Postgres

    protected lateinit var wireMock: WireMockServer

    @BeforeEach
    fun `stub defaults`() {
        wireMock.stubHttpEndpointForFindUserSucceeded()
    }

}
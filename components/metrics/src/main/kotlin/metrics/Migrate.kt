package metrics

import butler.IncorrectUsageException
import butler.Task
import com.beust.jcommander.Parameter
import org.flywaydb.core.Flyway
import java.nio.file.Path
import java.nio.file.Paths

class Migrate: Task {
    private val MYSQL = "mysql"
    private val POSTGRES = "postgres"
    override val name = "migrate"

    @Parameter(description = "database to migrate", required = true)
    var database: String? = null

    @Parameter(description = "clean", names = arrayOf("--clean", "-c"))
    var clean: Boolean = false

    @Parameter(description = "repair", names = arrayOf("--repair", "-r"))
    var repair: Boolean = false

    @Parameter(description = "port", names = arrayOf("--port"))
    var port: Int? = null

    @Parameter(description = "username", names = arrayOf("--username", "-u"))
    var username: String? = null

    @Parameter(description = "password", names = arrayOf("--password"))
    var password: String? = null

    override fun run() {
        val db = database
        if (db === null) {
            throw IncorrectUsageException("migrate command must specify a database to migrate")
        }

        if (!(db == MYSQL || db == POSTGRES)) {
            throw IncorrectUsageException("unknown database $db, must be $MYSQL or $POSTGRES")
        }

        val (url, migrationsPath) = buildFlywayEnvironmentForDbAndEnvironment(db)

        val flyway = Flyway()
        val username = username?: System.getenv("DEFAULT_DB_USERNAME")
        val password = password?: System.getenv("DEFAULT_DB_PASSWORD")

        flyway.setDataSource(url, username, password)
        flyway.setLocations("filesystem:${migrationsPath.toAbsolutePath().toString()}")

        if (clean) flyway.clean()
        if (repair) flyway.repair()

        flyway.migrate()
    }

    private fun buildFlywayEnvironmentForDbAndEnvironment(db: String): FlywayEnvironmentForDb {
        val host = "127.0.0.1"
        return if (db == MYSQL) {
            val port = port?: "3346"
            val url = "jdbc:mysql://$host:$port/metrics"
            val migrationsPath = getMetricsDataMigrationPath("dbmigrations")

            FlywayEnvironmentForDb(url, migrationsPath)
        } else {
            val port = port?: "5442"
            val url = "jdbc:postgresql://$host:$port/metrics"
            val migrationsPath = getMetricsDataMigrationPath("pgmigrations")

            FlywayEnvironmentForDb(url, migrationsPath)
        }
    }

    private fun getMetricsDataMigrationPath(migrationsFolder: String): Path {
        return Paths.get(
            System.getenv("HOME"),
            "workspace",
            "metrics-app-dev-release",
            "src",
            "github.com",
            "pivotal-cf",
            "metrics-data",
            migrationsFolder
        )
    }
}

data class FlywayEnvironmentForDb(
    val url: String,
    val migrationsPath: Path
)

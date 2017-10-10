package metrics

import bosh.buildLocalBoshProcess
import butler.IncorrectUsageException
import butler.Task
import com.beust.jcommander.Parameter
import java.nio.file.Path
import java.nio.file.Paths

class UploadMetricsAppDevRelease : Task {
    override val name = "upload-metrics-release"

    @Parameter(description = "guid", required = true, names = arrayOf("--version", "-v"))
    var version: String? = null

    override fun run() {
        val v = version ?: throw IncorrectUsageException("$name task requires a version")

        val tarballPath = Paths.get(
            metricsAppDevReleasePath().toString(),
            "dev_releases",
            "pcf-metrics-app-dev-release",
            "pcf-metrics-app-dev-release-$v.tgz"
        )

        val boshCommandParts = arrayOf<String>(
            "upload-release",
            tarballPath.toString()
        )

        val boshProcessBuilder = buildLocalBoshProcess(
            boshCommandParts,
            null,
            Versace.versaceBblPath()
        )

        val process = boshProcessBuilder
            .directory(metricsAppDevReleasePath().toFile())
            .inheritIO()
            .start()

        process.waitFor()
    }

    private fun metricsAppDevReleasePath(): Path {
        return Paths.get(
            System.getenv("HOME"), "workspace", "metrics-app-dev-release"
        )
    }

}

package metrics

import bosh.buildAndWaitForLocalBoshProcess
import butler.BoshEnvironment
import java.nio.file.Path
import java.nio.file.Paths

class Versace : BoshEnvironment {
    override val name = "versace"
    override val nickname = "v"

    override fun bosh(boshCommand: String, deployment: String?, username: String?) {
        val boshCommandParts = boshCommand.split(" ").toTypedArray()
        buildAndWaitForLocalBoshProcess(
            boshCommandParts,
            deployment,
            versaceBblPath()
        )
    }

    companion object {
        fun versaceBblPath(): Path {
            return Paths.get(
                System.getenv("HOME"), "workspace", "deployments-metrics", "gcp-environments", "versace", "bbl-state.json"
            )
        }
    }
}

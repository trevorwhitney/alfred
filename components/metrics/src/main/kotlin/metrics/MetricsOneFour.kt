package metrics

import bosh.buildAndWaitForLocalBoshProcess
import butler.SshEnvironment
import metrics.Versace.Companion.versaceBblPath

class MetricsOneFour : SshEnvironment {
    override val name = "metrics-14"
    override val nickname = "m14"

    override fun ssh(vm: String, username: String?) {
        buildAndWaitForLocalBoshProcess(
            arrayOf("ssh", vm),
            "pcf-metrics-v1.4",
            versaceBblPath()
        )
    }
}

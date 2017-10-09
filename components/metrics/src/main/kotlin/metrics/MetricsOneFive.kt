package metrics

import bosh.buildAndWaitForLocalBoshProcess
import butler.SshEnvironment
import metrics.Versace.Companion.versaceBblPath

class MetricsOneFive : SshEnvironment {
    override val name = "metrics-15"
    override val nickname = "m15"

    override fun ssh(vm: String, username: String?) {
        buildAndWaitForLocalBoshProcess(
            arrayOf("ssh", vm),
            "pcf-metrics-v1.5",
            versaceBblPath()
        )
    }
}

package metrics

import bosh.buildAndWaitForLocalBoshProcess
import butler.Bosh
import butler.BoshEnvironment
import butler.Ssh


@BoshEnvironment("metrics-14", nickname = "m14")
class MetricsOneFour {
    @Ssh
    fun ssh(vm: String) {
        buildAndWaitForLocalBoshProcess(
            arrayOf("ssh", vm),
            "pcf-metrics-v1.4",
            versaceBblPath()
        )
    }

    @Bosh
    fun bosh(boshCommand: String) {
        val boshCommandParts = boshCommand.split(" ").toTypedArray()
        buildAndWaitForLocalBoshProcess(
            boshCommandParts,
            "pcf-metrics-v1.4",
            versaceBblPath()
        )
    }
}

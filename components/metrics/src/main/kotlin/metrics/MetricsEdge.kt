package metrics

import bosh.buildAndWaitForRemoteBoshProcess
import butler.Bosh
import butler.BoshEnvironment
import butler.Ssh

@BoshEnvironment("metrics-edge", nickname = "me")
class MetricsEdge {
    @Ssh
    fun ssh(vm: String, username: String) {
        buildAndWaitForRemoteBoshProcess(
            username,
            System.getenv("PROD_HOST"),
            prodRemoteCommand("pcf-metrics-edge", "ssh $vm")
        )
    }

    @Bosh
    fun bosh(boshCommand: String, username: String) {
        buildAndWaitForRemoteBoshProcess(
            username,
            System.getenv("PROD_HOST"),
            prodRemoteCommand("pcf-metrics-edge", boshCommand)
        )

    }
}

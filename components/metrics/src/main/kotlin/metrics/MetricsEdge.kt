package metrics

import bosh.buildAndWaitForRemoteBoshProcess
import butler.IncorrectUsageException
import butler.SshEnvironment
import metrics.Prod.Companion.prodRemoteCommand

class MetricsEdge : SshEnvironment {
    override val name = "metrics-edge"
    override val nickname = "me"

    override fun ssh(vm: String, username: String?) {
        if (username == null) {
            throw IncorrectUsageException("$name environment requires a username")
        }

        buildAndWaitForRemoteBoshProcess(
            username,
            System.getenv("PROD_HOST"),
            prodRemoteCommand("pcf-metrics-edge", "ssh $vm")
        )
    }
}

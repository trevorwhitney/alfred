package metrics

import bosh.buildAndWaitForRemoteBoshProcess
import butler.IncorrectUsageException
import butler.SshEnvironment
import metrics.Prod.Companion.prodRemoteCommand

class MetricsProd : SshEnvironment {
    override val name = "metrics-prod"
    override val nickname = "mp"

    override fun ssh(vm: String, username: String?) {
        if (username == null) {
            throw IncorrectUsageException("$name environment requires a username")
        }

        buildAndWaitForRemoteBoshProcess(
            username,
            System.getenv("PROD_HOST"),
            prodRemoteCommand("pcf-metrics-prod", "ssh $vm")
        )
    }
}

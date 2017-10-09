package metrics

import bosh.buildAndWaitForRemoteBoshProcess
import butler.BoshEnvironment
import butler.IncorrectUsageException

class Prod : BoshEnvironment {
    override val name = "prod"
    override val nickname = "p"

    override fun bosh(boshCommand: String, deployment: String?, username: String?) {
        if (username == null) {
            throw IncorrectUsageException("$name environment requires a username")
        }

        buildAndWaitForRemoteBoshProcess(
            username,
            System.getenv("PROD_HOST"),
            prodRemoteCommand(deployment, boshCommand)
        )
    }

    companion object {
        fun prodRemoteCommand(deployment: String?, boshCommand: String): String {
            if (deployment == null) {
                return "bash -l -c \"direnv allow && gobosh -e prod $boshCommand\""
            }

            return "bash -l -c \"direnv allow && gobosh -e prod -d $deployment $boshCommand\""
        }
    }
}



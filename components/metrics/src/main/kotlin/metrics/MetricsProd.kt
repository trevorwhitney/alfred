package metrics

import butler.BoshEnvironment
import butler.Ssh

@BoshEnvironment(names = arrayOf("metrics-prod", "mp"))
class MetricsProd {
    @Ssh
    fun ssh(vm: String, username: String) {
        val prodHost = System.getenv("PROD_HOST")
        val command = listOf("ssh", "$username@$prodHost", "-t", "bash -l -c \"direnv allow && gobosh -e prod -d pcf-metrics-prod ssh $vm\"")
        val process = ProcessBuilder()
            .command(command)
            .inheritIO()
            .start()

        process.waitFor()
    }
}

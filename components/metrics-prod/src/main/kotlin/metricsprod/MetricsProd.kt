package metricsprod

import butler.CommandCollection
import butler.SshCommand

@CommandCollection(names = arrayOf("metrics-prod", "mp"))
class MetricsProd {
    @SshCommand
    fun ssh(username: String, vm: String) {
        val prodHost = System.getenv("PROD_HOST")
        val process = ProcessBuilder()
            .command(listOf("ssh", "$username@$prodHost", "-t", "bash -l -c \"direnv allow && gobosh -e prod -d pcf-metrics-prod ssh $vm\""))
            .inheritIO()
            .start()

        process.waitFor()
    }
}

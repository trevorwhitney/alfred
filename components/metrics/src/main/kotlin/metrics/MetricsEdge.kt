package metrics

import butler.CommandCollection
import butler.SshCommand

@CommandCollection(names = arrayOf("metrics-edge", "me"))
class MetricsEdge {
    @SshCommand
    fun ssh(vm: String, username: String) {
        val prodHost = System.getenv("PROD_HOST")
        val command = listOf("ssh", "$username@$prodHost", "-t", "bash -l -c \"direnv allow && gobosh -e prod -d pcf-metrics-edge ssh $vm\"")
        val process = ProcessBuilder()
            .command(command)
            .inheritIO()
            .start()

        process.waitFor()
    }
}

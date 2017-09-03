package alfred

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException


class GlobalArgs {}

class SshMetricsProdCommand {
    @Parameter(names = arrayOf("--username", "-u"), description = "Your username for prod", required = true)
    var username: String? = null

    @Parameter(names = arrayOf("-vm"), description = "VM to SSH to", required = true)
    var vm: String? = null

    fun execute(username: String, vm: String) {
        val prodHost = System.getenv("PROD_HOST")
        val process = ProcessBuilder()
                .command(listOf("ssh", "$username@$prodHost", "-t", "bash -l -c \"direnv allow && gobosh -e prod -d pcf-metrics-prod ssh $vm\""))
                .inheritIO()
                .start()

        process.waitFor()
    }
}

data class BoshConfig(
        val directorAddress: String, //$(bbl director-address)
        val directorCaCert: String, //$(bbl director-ca-cert)
        val directorUsername: String, //$(bbl director-username)
        val directorPassword: String, //$(bbl director-password)
        val gatewayUser: String, //jumpbox
        val gatewayHost: String, //$(bbl director-address | sed -e "s/^https:\/\///" -e "s/:25555$//")
        val gatewayPrivateKey: String //bbl ssh-key > /tmp/ssh.pem
)

class SshVersace {
    fun execute(vm: String) {
        val process = ProcessBuilder()
                .command(listOf("bosh2", "-e", "versace", "-d", "deployment", "ssh", vm))
                .inheritIO()
                .start()

        process.waitFor()
    }
}

fun main(args: Array<String>) {
    val sshMetricsProdCommand = SshMetricsProdCommand()

    val jc = JCommander
            .newBuilder()
            .addObject(GlobalArgs())
            .addCommand("ssh-metrics-prod", sshMetricsProdCommand, "smp")
            .build()

    jc.programName = "Alfred"
    try {
        jc.parse(*args)
    } catch (e: ParameterException) {
        jc.usage()
        //TODO: move this to a debug mode
        e.printStackTrace()
        System.exit(1)
    }

    when (jc.parsedCommand) {
        "ssh-metrics-prod" -> {
            val username = sshMetricsProdCommand.username
            val vm = sshMetricsProdCommand.vm

            if (username === null || vm === null) {
                jc.usage()
                System.exit(1)
            }

            sshMetricsProdCommand.execute(username!!, vm!!)
        }
        else -> {
            jc.usage()
            System.exit(1)
        }
    }
}

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

class SshVersace {
    /*
    * pushd $ENVIRONMENT_DIR
    * export BOSH_ENVIRONMENT=$(bbl director-address)
    * export BOSH_CA_CERT=$(bbl director-ca-cert)
    * export BOSH_CLIENT=$(bbl director-username)
    * export BOSH_CLIENT_SECRET=$(bbl director-password)
    * export BOSH_GW_USER=jumpbox
    * export BOSH_GW_HOST=$(bbl director-address | sed -e "s/^https:\/\///" -e "s/:25555$//")
    * bbl ssh-key > /tmp/ssh.pem
    * chmod 600 /tmp/ssh.pem
    * export BOSH_GW_PRIVATE_KEY=/tmp/ssh.pem
    * */

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

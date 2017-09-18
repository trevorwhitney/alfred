package butler

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

data class EnvironmentInfo(
    val longName: String,
    val shortName: String,
    var supportsSsh: Boolean = false,
    var supportsBosh: Boolean = false
)

//TODO: make list of names a tuple
fun buildEnvironmentInfo(names: List<String>): EnvironmentInfo {
    val longName = names.fold("", fun(acc: String, string: String): String {
        return if (acc.length > string.length) acc else string
    })

    return EnvironmentInfo(
        longName = longName,
        shortName = "foo" //TODO: fix
    )
}

class ButlerBuilder {
    var name: String? = null
    private val sshCommands = mutableMapOf<String, (String, String?) -> Unit>()
    private val boshCommands = mutableMapOf<String, (String, String?) -> Unit>()
    private val environments = mutableMapOf<String, MutableList<String>>()

    fun registerSsh(environmentNames: Array<String>, sshCommand: (String, String?) -> Unit) {
        environmentNames.forEach {
            registerSsh(it, sshCommand)
        }
    }

    private fun registerSsh(environmentName: String, sshCommand: (String, String?) -> Unit) {
        sshCommands.put(environmentName, sshCommand)
        if (environments.containsKey(environmentName)) {
            environments[environmentName]?.add("ssh")
            return
        }

        environments[environmentName] = mutableListOf("ssh")
    }

    fun registerBosh(environmentNames: Array<String>, boshMethod: (String, String?) -> Unit) {
        environmentNames.forEach {
            registerBosh(it, boshMethod)
        }
    }

    private fun registerBosh(environmentName: String, boshMethod: (String, String?) -> Unit) {
        boshCommands.put(environmentName, boshMethod)
        if (environments.containsKey(environmentName)) {
            environments[environmentName]?.add("bosh")
            return
        }

        environments[environmentName] = mutableListOf("bosh")
    }

    fun build(): (args: Array<String>) -> Unit {
        val sshOptions = SshOptions()
        val boshOptions = BoshOptions()

        val jc: JCommander = JCommander
            .newBuilder()
            .addCommand("ssh", sshOptions)
            .addCommand("bosh", boshOptions)
            .build()

        jc.programName = name

        return fun(args: Array<String>) {
            try {
                jc.parse(*args)
            } catch (e: ParameterException) {
                jc.usage()
                //TODO: move this to a debug/verbose mode. Use a logger?
                e.printStackTrace()
                return System.exit(1)
            }

            when (jc.parsedCommand) {
                "ssh" -> {
                    try {
                        if (!ssh(jc, sshOptions)) {
                            jc.usage()
                            return System.exit(1)
                        }
                    } catch (e: IncorrectUsageException) {
                        println(e.message)
                        return System.exit(1)
                    }

                }
                "bosh" -> {
                    try {
                        if (!bosh(jc, boshOptions)) {
                            jc.usage()
                            return System.exit(1)
                        }
                    } catch (e: IncorrectUsageException) {
                        println(e.message)
                        return System.exit(1)
                    }
                }
                else -> {
                    jc.usage()
                    System.exit(1)
                }
            }
        }
    }

    private fun bosh(jc: JCommander, boshOptions: BoshOptions): Boolean {
        val env = boshOptions.environment
        if (env === null) {
            return false
        }

        val boshCommand = boshOptions.command
        if (boshCommand === null) {
            return false
        }

        if (!boshCommands.containsKey(env)) {
            println("$env environment does not support bosh actions")
            return false
        }

        val command: ((String, String?) -> Unit)? = boshCommands[env]
        if (command === null) {
            return false
        }

        command(boshCommand, boshOptions.username)
        return true
    }

    private fun ssh(jc: JCommander, sshOptions: SshOptions): Boolean {
        val env = sshOptions.environment
        if (env === null) {
            return false
        }

        val vm = sshOptions.vm
        if (vm === null) {
            return false
        }

        if (!sshCommands.containsKey(env)) {
            println("$env environment does not support ssh actions")
            return false
        }

        val command: ((String, String?) -> Unit)? = sshCommands[env]
        if (command === null) {
            return false
        }

        command(vm, sshOptions.username)
        return true
    }
}

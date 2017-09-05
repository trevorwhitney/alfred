package butler

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

class ButlerBuilder {
    var name: String? = null
    private val sshCommands = mutableMapOf<String, (String, String?) -> Unit>()

    fun registerSsh(environmentNames: Array<String>, sshCommand: (String, String?) -> Unit) {
        environmentNames.forEach {
            registerSsh(it, sshCommand)
        }
    }

    private fun registerSsh(environmentName: String, sshCommand: (String, String?) -> Unit) {
        sshCommands.put(environmentName, sshCommand)
    }

    fun build(): (args: Array<String>) -> Unit {
        val sshOptions = SshOptions()

        val jc = JCommander
                .newBuilder()
                .addCommand("ssh", sshOptions)
                .build()

        jc.programName = name

        return fun(args: Array<String>) {
            try {
                jc.parse(*args)
            } catch (e: ParameterException) {
                jc.usage()
                //TODO: move this to a debug mode
                e.printStackTrace()
                return System.exit(1)
            }

            when (jc.parsedCommand) {
                "ssh" -> {
                    val env = sshOptions.environment
                    if (env === null) {
                        jc.usage()
                        return System.exit(1)
                    }

                    val vm = sshOptions.vm

                    if (vm === null) {
                        jc.usage()
                        return System.exit(1)
                    }

                    if (!sshCommands.containsKey(env)) {
                        println("No ssh command defined for $env")
                        return System.exit(1)
                    }

                    val command: ((String, String?) -> Unit)? = sshCommands[env]
                    if (command === null) {
                        jc.usage()
                        return System.exit(1)
                    }
                    try {
                        command(vm, sshOptions.username)
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
}

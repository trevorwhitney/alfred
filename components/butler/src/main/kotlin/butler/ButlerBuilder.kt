package butler

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

class ButlerBuilder {
    var name: String? = null
    private val sshEnvironments = mutableMapOf<String, SshEnvironment>()
    private val boshEnvironments = mutableMapOf<String, BoshEnvironment>()
    private val tasks = mutableMapOf<String, Task>()

    private val environments = mutableMapOf<String, EnvironmentInfo>()

    fun registerSsh(sshEnvironment: SshEnvironment) {
        val environmentName = sshEnvironment.name
        val environmentNickname = sshEnvironment.nickname

        sshEnvironments.put(environmentName, sshEnvironment)
        sshEnvironments.put(environmentNickname, sshEnvironment)

        if (environments.containsKey(environmentName)) {
            environments[environmentName]?.supportsSsh = true
            return
        }

        environments[environmentName] = buildEnvironmentInfo(environmentName, environmentNickname).apply {
            supportsSsh = true
        }
    }

    fun registerBosh(boshEnvironment: BoshEnvironment) {
        val environmentName = boshEnvironment.name
        val environmentNickname = boshEnvironment.nickname

        boshEnvironments.put(environmentName, boshEnvironment)
        boshEnvironments.put(environmentNickname, boshEnvironment)

        if (environments.containsKey(environmentName)) {
            environments[environmentName]?.supportsBosh = true
            return
        }

        environments[environmentName] = buildEnvironmentInfo(environmentName, environmentNickname).apply {
            supportsBosh = true
        }
    }

    fun registerTask(task: Task) {
        tasks.put(task.name, task)
    }

    fun build(): (args: Array<String>) -> Unit {
        val sshOptions = SshOptions()
        val boshOptions = BoshOptions()
        val listEnvironmentsOptions = ListEnvironmentsOptions()

        val builder = JCommander
            .newBuilder()
            .addCommand("ssh", sshOptions)
            .addCommand("bosh", boshOptions)
            .addCommand("environments", listEnvironmentsOptions)

        tasks.forEach { k, v ->
            builder.addCommand(k, v)
        }

        val jc: JCommander = builder
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
                "environments" -> {
                    println("Supported Commands by Environment")
                    println("---------------------------------")
                    environments.forEach { k, v ->
                        val supportedEnvironments = listOf(
                            Pair(v.supportsBosh, "bosh"),
                            Pair(v.supportsSsh, "ssh")
                        ).filter { it.first }.map { it.second }

                        println("$k:\t${supportedEnvironments.joinToString(", ")}")
                    }
                }
                else -> {
                    if (!tasks.containsKey(jc.parsedCommand)) {
                        jc.usage()
                        return System.exit(1)
                    }

                    val task = tasks[jc.parsedCommand]
                    if (task === null) {
                        return System.exit(3)
                    }

                    task.run()
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

        if (!boshEnvironments.containsKey(env)) {
            println("$env environment does not support bosh actions")
            return false
        }

        val boshEnvironment = boshEnvironments[env]
        if (boshEnvironment === null) {
            return false
        }

        boshEnvironment.bosh(boshCommand, boshOptions.deployment, boshOptions.username)
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

        if (!sshEnvironments.containsKey(env)) {
            println("$env environment does not support ssh actions")
            return false
        }

        val sshEnvironment = sshEnvironments[env]
        if (sshEnvironment === null) {
            return false
        }

        sshEnvironment.ssh(vm, sshOptions.username)
        return true
    }
}

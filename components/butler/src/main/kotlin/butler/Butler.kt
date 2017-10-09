package butler

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

class Butler(val name: String) {
    private final val builder = ButlerBuilder().apply {
        this.name = name
    }

    init {
        scanForBoshEnvironments()
        scanForSshEnvironments()
        scanForTasks()
    }

    private fun scanForBoshEnvironments() {
        val boshEnvironmentScanner = ClassPathScanningCandidateComponentProvider(false)
        boshEnvironmentScanner.addIncludeFilter(AssignableTypeFilter(BoshEnvironment::class.java))

        boshEnvironmentScanner.findCandidateComponents("metrics").forEach {
            val clazz = Class.forName(it.beanClassName)
            val instance = clazz.newInstance() as BoshEnvironment

            builder.registerBosh(instance)
        }

    }

    private fun scanForSshEnvironments() {
        val sshEnvironmentScanner = ClassPathScanningCandidateComponentProvider(false)
        sshEnvironmentScanner.addIncludeFilter(AssignableTypeFilter(SshEnvironment::class.java))

        sshEnvironmentScanner.findCandidateComponents("metrics").forEach {
            val clazz = Class.forName(it.beanClassName)
            val instance = clazz.newInstance() as SshEnvironment

            builder.registerSsh(instance)
        }
    }

    private fun scanForTasks() {
        val taskScanner = ClassPathScanningCandidateComponentProvider(false)
        taskScanner.addIncludeFilter(AssignableTypeFilter(Task::class.java))

        taskScanner.findCandidateComponents("metrics").forEach {
            val clazz = Class.forName(it.beanClassName)
            val task: Task = clazz.newInstance() as Task

            builder.registerTask(task)
        }
    }

    fun exec(args: Array<String>) {
        builder.build()(args)
    }
}

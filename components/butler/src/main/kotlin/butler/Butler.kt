package butler

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import java.lang.reflect.Method

class Butler(val name: String) {
    private final val builder = ButlerBuilder().apply {
        this.name = name
    }

    init {
        scanForBoshEnvironments()
        scanForTasks()
    }

    private fun scanForBoshEnvironments() {
        val boshEnvironmentScanner = ClassPathScanningCandidateComponentProvider(false)
        boshEnvironmentScanner.addIncludeFilter(AnnotationTypeFilter(BoshEnvironment::class.java))

        boshEnvironmentScanner.findCandidateComponents("metrics").forEach {
            val clazz = Class.forName(it.beanClassName)
            val instance = clazz.newInstance()
            val boshEnvironment = AnnotationUtils.findAnnotation(clazz, BoshEnvironment::class.java)
            val environmentName: String = AnnotationUtils.getValue(boshEnvironment, "name").toString()
            val environmentNickname: String = AnnotationUtils.getValue(boshEnvironment, "nickname").toString()
            val methods = clazz.declaredMethods

            methods.map {
                val sshCommand = AnnotationUtils.getAnnotation(it, Ssh::class.java)
                val boshCommand = AnnotationUtils.getAnnotation(it, Bosh::class.java)

                val parameterTypes = it.parameterTypes

                if (sshCommand !== null) {
                    addSshMethod(it, environmentName, environmentNickname, instance)
                } else if (boshCommand !== null) {
                    addBoshMethod(it, environmentName, environmentNickname, instance)
                }
            }
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

    private fun addBoshMethod(boshMethod: Method, environmentName: String, environmentNickname: String, instance: Any?) {
        val parameterTypes = boshMethod.parameterTypes
        val isRemoteBoshCommand = boshMethod.parameterCount == 2
            && parameterTypes[0] == String::class.java
            && parameterTypes[0] == String::class.java

        builder.registerBosh(environmentName, environmentNickname, fun(boshCommand: String, username: String?): Unit {
            if (isRemoteBoshCommand) {
                if (username !== null) {
                    boshMethod.invoke(instance, boshCommand, username)
                } else {
                    throw IncorrectUsageException("Bosh command for $environmentName environment requires a username")
                }
            } else if (parameterTypes[0] == String::class.java && boshMethod.parameterCount == 1) {
                boshMethod.invoke(instance, boshCommand)
            } else {
                throw IncorrectUsageException("Bosh command definition for $environmentName environment must take a bosh command")
            }
        })
    }

    private fun addSshMethod(sshMethod: Method, environmentName: String, environmentNickname: String, instance: Any?) {
        val parameterTypes = sshMethod.parameterTypes
        val isRemoteSshCommand = sshMethod.parameterCount == 2
            && parameterTypes[0] == String::class.java
            && parameterTypes[0] == String::class.java

        builder.registerSsh(environmentName, environmentNickname, fun(vm: String, username: String?): Unit {
            if (isRemoteSshCommand) {
                if (username !== null) {
                    sshMethod.invoke(instance, vm, username)
                } else {
                    throw IncorrectUsageException("Ssh command for $environmentName environment requires a username")
                }
            } else if (parameterTypes[0] == String::class.java && sshMethod.parameterCount == 1) {
                sshMethod.invoke(instance, vm)
            } else {
                throw IncorrectUsageException("Ssh command definition for $environmentName environment must at least take a VM name")
            }
        })
    }

    fun exec(args: Array<String>) {
        builder.build()(args)
    }
}

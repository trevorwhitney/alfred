package butler

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter
import java.lang.reflect.Method

class Butler(val name: String) {
    private final val builder = ButlerBuilder().apply {
        this.name = name
    }
    private final val scanner = ClassPathScanningCandidateComponentProvider(false)

    init {
        scanner.addIncludeFilter(AnnotationTypeFilter(BoshEnvironment::class.java))

        scanner.findCandidateComponents("metrics").forEach {
            val clazz = Class.forName(it.beanClassName)
            val instance = clazz.newInstance()
            val commandCollection = AnnotationUtils.findAnnotation(clazz, BoshEnvironment::class.java)
            val environmentAliases = AnnotationUtils.getValue(commandCollection, "names") as Array<String>
            val methods = clazz.declaredMethods

            methods.map {
                val sshCommand = AnnotationUtils.getAnnotation(it, Ssh::class.java)
                val boshCommand = AnnotationUtils.getAnnotation(it, Bosh::class.java)

                val parameterTypes = it.parameterTypes

                if (sshCommand !== null) {
                    addSshMethod(it, environmentAliases, instance)
                } else if (boshCommand !== null) {
                    addBoshMethod(it, environmentAliases, instance)
                }
            }
        }
    }

    private fun addBoshMethod(boshMethod: Method, collectionNames: Array<String>, instance: Any?) {
        val parameterTypes = boshMethod.parameterTypes
        val isRemoteBoshCommand = boshMethod.parameterCount == 2
            && parameterTypes[0] == String::class.java
            && parameterTypes[0] == String::class.java
        val environments = collectionNames.joinToString(", ")

        builder.registerBosh(collectionNames, fun(boshCommand: String, username: String?): Unit {
            if (isRemoteBoshCommand) {
                if (username !== null) {
                    boshMethod.invoke(instance, boshCommand, username)
                } else {
                    throw IncorrectUsageException("Bosh command for environment: [$environments] requires a username")
                }
            } else if (parameterTypes[0] == String::class.java && boshMethod.parameterCount == 1) {
                boshMethod.invoke(instance, boshCommand)
            } else {
                throw IncorrectUsageException("Bosh command definition for environment: [$environments] must take a bosh command")
            }
        })
    }

    private fun addSshMethod(sshMethod: Method, collectionNames: Array<String>, instance: Any?) {
        val parameterTypes = sshMethod.parameterTypes
        val isRemoteSshCommand = sshMethod.parameterCount == 2
            && parameterTypes[0] == String::class.java
            && parameterTypes[0] == String::class.java
        val environments = collectionNames.joinToString(", ")

        builder.registerSsh(collectionNames, fun(vm: String, username: String?): Unit {
            if (isRemoteSshCommand) {
                if (username !== null) {
                    sshMethod.invoke(instance, vm, username)
                } else {
                    throw IncorrectUsageException("Ssh command for environment: [$environments] requires a username")
                }
            } else if (parameterTypes[0] == String::class.java && sshMethod.parameterCount == 1) {
                sshMethod.invoke(instance, vm)
            } else {
                throw IncorrectUsageException("Ssh command definition for environment: [$environments] must at least take a VM name")
            }
        })
    }

    fun exec(args: Array<String>) {
        builder.build()(args)
    }
}

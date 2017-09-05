package butler

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter

class Butler(val name: String) {
    private final val builder = ButlerBuilder().apply {
        this.name = name
    }
    private final val scanner = ClassPathScanningCandidateComponentProvider(false)

    init {
        scanner.addIncludeFilter(AnnotationTypeFilter(CommandCollection::class.java))

        scanner.findCandidateComponents("metricsprod").forEach {
            val clazz = Class.forName(it.beanClassName)
            val instance = clazz.newInstance()
            val commandCollection = AnnotationUtils.findAnnotation(clazz, CommandCollection::class.java)
            val collectionNames = AnnotationUtils.getValue(commandCollection, "names") as Array<String>
            val methods = clazz.declaredMethods

            methods.map {
                val sshCommand = AnnotationUtils.getAnnotation(it, SshCommand::class.java)
                val boshCommand = AnnotationUtils.getAnnotation(it, BoshCommand::class.java)

                val parameterTypes = it.parameterTypes

                if (sshCommand !== null) {
                    val isRemoteSshCommand = it.parameterCount == 2
                            && parameterTypes[0] == String::class.java
                            && parameterTypes[0] == String::class.java
                    val environments = collectionNames.joinToString(", ")

                    builder.registerSsh(collectionNames, fun(vm: String, username: String?): Unit {
                        if (isRemoteSshCommand) {
                            if (username !== null) {
                                it.invoke(instance, vm, username)
                            } else {
                                throw IncorrectUsageException("Ssh command for environment: [$environments] requires a username")
                            }
                        } else if (parameterTypes[0] == String::class.java && it.parameterCount == 1) {
                            it.invoke(instance, vm)
                        } else {
                            throw IncorrectUsageException("Ssh command definition for environment: [$environments] must at least take a VM name")
                        }
                    })
                } else if (boshCommand !== null) {

                }
            }
        }
    }

    fun exec(args: Array<String>) {
        builder.build()(args)
    }
}

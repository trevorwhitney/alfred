package alfred

import butler.CommandCollection
import com.beust.jcommander.Parameter
import metricsprod.MetricsProd
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter

class GlobalArgs {}

fun sshMetricsProd(username: String, vm: String) {
    val prodHost = System.getenv("PROD_HOST")
    val process = ProcessBuilder()
        .command(listOf("ssh", "$username@$prodHost", "-t", "bash -l -c \"direnv allow && gobosh -e prod -d pcf-metrics-prod ssh $vm\""))
        .inheritIO()
        .start()

    process.waitFor()
}

fun sshVersace(username: String, vm: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

class SshCommand {
    @Parameter(description = "Environment VM is in")
    var environment: String? = null

    @Parameter(names = arrayOf("--username", "-u"), description = "Your username for prod", required = true)
    var username: String? = null

    @Parameter(names = arrayOf("-vm"), description = "VM to SSH to", required = true)
    var vm: String? = null

    fun expandShorthandEnv(): String? {
        return when (environment) {
            "mp" -> "metrics-prod"
            else -> environment
        }
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

enum class Environment(environment: String) {
    METRICS_PROD("metrics-prod") {
        override fun ssh(username: String, vm: String) {
            sshMetricsProd(username, vm)
        }
    },
    VERSACE("versace") {
        override fun ssh(username: String, vm: String) {
            sshVersace(username, vm)
        }
    };

    abstract fun ssh(username: String, vm: String)
}


fun main(args: Array<String>) {
    val scanner = ClassPathScanningCandidateComponentProvider(false)

    scanner.addIncludeFilter(AnnotationTypeFilter(CommandCollection::class.java))

    scanner.findCandidateComponents("metricsprod").forEach {
        println(it.beanClassName)
        val clazz = Class.forName(it.beanClassName)
        val commandCollection = AnnotationUtils.findAnnotation(clazz, CommandCollection::class.java)
        val collectionNames = AnnotationUtils.getValue(commandCollection, "names")
        val methods = clazz.declaredMethods

        methods.map {
            it.isAnnotationPresent(SshCommand::class.java)
        }

        println("Declared Methods: $methods")
    }


//    val sshCommand = SshCommand()
//
//    val jc = JCommander
//        .newBuilder()
//        .addObject(GlobalArgs())
//        .addCommand("ssh", sshCommand)
//        .build()
//
//    jc.programName = "Alfred"
//    try {
//        jc.parse(*args)
//    } catch (e: ParameterException) {
//        jc.usage()
//        //TODO: move this to a debug mode
//        e.printStackTrace()
//        return System.exit(1)
//    }
//
//    when (jc.parsedCommand) {
//        "ssh" -> {
//            val env = sshCommand.expandShorthandEnv()
//            if (env === null) {
//                jc.usage()
//                return System.exit(1)
//            }
//
//            val environment = Environment.valueOf(env)
//            val username = sshCommand.username
//            val vm = sshCommand.vm
//
//            if (username === null || vm === null) {
//                jc.usage()
//                return System.exit(1)
//            }
//
//            environment.ssh(username, vm)
//        }
//        else -> {
//            jc.usage()
//            System.exit(1)
//        }
//    }
}

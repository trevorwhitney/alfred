package metrics

import bosh.loadDirectorConfig
import butler.Bosh
import butler.BoshEnvironment
import butler.Ssh
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.HashSet



@BoshEnvironment("metrics-14", nickname = "m14")
class MetricsOneFour {
    @Ssh
    fun ssh(vm: String) {
        val home = System.getenv("HOME")
        val versacePath = Paths.get(home, "workspace", "deployments-metrics", "gcp-environments", "versace", "bbl-state.json")
        val (directorAddress,
            directorCaCert,
            directorUsername,
            directorPassword,
            gatewayUser,
            gatewayHost,
            gatewayPrivateKey
            ) = loadDirectorConfig(versacePath.toString())

        val command = listOf("bosh2", "-d", "pcf-metrics-v1.4", "ssh", vm)
        val processBuilder = ProcessBuilder()
            .command(command)

        val environment = processBuilder.environment()
        environment.put("BOSH_ENVIRONMENT", directorAddress)
        environment.put("BOSH_CA_CERT", directorCaCert)
        environment.put("BOSH_CLIENT", directorUsername)
        environment.put("BOSH_CLIENT_SECRET", directorPassword)
        environment.put("BOSH_GW_USER", gatewayUser)
        environment.put("BOSH_GW_HOST", gatewayHost)

        val privateKey = File.createTempFile("ssh", ".pem")
        privateKey.writeText(gatewayPrivateKey)

        val perms = HashSet<PosixFilePermission>()
        perms.add(PosixFilePermission.OWNER_READ)
        perms.add(PosixFilePermission.OWNER_WRITE)
        Files.setPosixFilePermissions(privateKey.toPath(), perms)

        environment.put("BOSH_GW_PRIVATE_KEY", privateKey.absolutePath)

        val process = processBuilder
            .inheritIO()
            .start()

        process.waitFor()
    }

    @Bosh
    fun bosh(boshCommand: String) {
        val home = System.getenv("HOME")
        val versacePath = Paths.get(home, "workspace", "deployments-metrics", "gcp-environments", "versace", "bbl-state.json")
        val (directorAddress,
            directorCaCert,
            directorUsername,
            directorPassword,
            gatewayUser,
            gatewayHost,
            gatewayPrivateKey
            ) = loadDirectorConfig(versacePath.toString())

        val command: List<String> = listOf("bosh2", "-d", "pcf-metrics-v1.4", "ssh", boshCommand)
        val processBuilder = ProcessBuilder()
            .command(command)

        val environment = processBuilder.environment()
        environment.put("BOSH_ENVIRONMENT", directorAddress)
        environment.put("BOSH_CA_CERT", directorCaCert)
        environment.put("BOSH_CLIENT", directorUsername)
        environment.put("BOSH_CLIENT_SECRET", directorPassword)
        environment.put("BOSH_GW_USER", gatewayUser)
        environment.put("BOSH_GW_HOST", gatewayHost)

        val privateKey = File.createTempFile("ssh", ".pem")
        privateKey.writeText(gatewayPrivateKey)

        val perms = HashSet<PosixFilePermission>()
        perms.add(PosixFilePermission.OWNER_READ)
        perms.add(PosixFilePermission.OWNER_WRITE)
        Files.setPosixFilePermissions(privateKey.toPath(), perms)

        environment.put("BOSH_GW_PRIVATE_KEY", privateKey.absolutePath)

        val process = processBuilder
            .inheritIO()
            .start()

        process.waitFor()
    }
}

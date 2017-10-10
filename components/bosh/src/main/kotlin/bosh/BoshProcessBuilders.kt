package bosh

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.HashSet

fun buildAndWaitForLocalBoshProcess(boshCommand: Array<String>, deployment: String?, bblPath: Path) {
    val processBuilder = buildLocalBoshProcess(boshCommand, deployment, bblPath)

    val process = processBuilder
        .inheritIO()
        .start()

    process.waitFor()
}

fun buildLocalBoshProcess(boshCommand: Array<String>, deployment: String?, bblPath: Path): ProcessBuilder {
    val (directorAddress,
        directorCaCert,
        directorUsername,
        directorPassword,
        gatewayUser,
        gatewayHost,
        gatewayPrivateKey
        ) = loadDirectorConfig(bblPath.toString())

    val command = mutableListOf<String>(
        "bosh",
        "-e", directorAddress,
        "--client", directorUsername,
        "--client-secret", directorPassword,
        *boshCommand)

    if (deployment != null) {
        command.addAll(listOf("-d", deployment))
    }

    val processBuilder = ProcessBuilder()
        .command(command)

    val environment = processBuilder.environment()
    environment.put("BOSH_CA_CERT", directorCaCert)
    environment.put("BOSH_GW_USER", gatewayUser)
    environment.put("BOSH_GW_HOST", gatewayHost)

    val privateKey = File.createTempFile("ssh", ".pem")
    privateKey.writeText(gatewayPrivateKey)

    val perms = HashSet<PosixFilePermission>()
    perms.add(PosixFilePermission.OWNER_READ)
    perms.add(PosixFilePermission.OWNER_WRITE)
    Files.setPosixFilePermissions(privateKey.toPath(), perms)

    environment.put("BOSH_GW_PRIVATE_KEY", privateKey.absolutePath)
    return processBuilder
}

fun buildAndWaitForRemoteBoshProcess(username: String, remoteHost: String, remoteCommand: String) {
    val command = listOf(
        "ssh",
        "$username@$remoteHost",
        "-t",
        remoteCommand)

    val process = ProcessBuilder()
        .command(command)
        .inheritIO()
        .start()

    process.waitFor()
}

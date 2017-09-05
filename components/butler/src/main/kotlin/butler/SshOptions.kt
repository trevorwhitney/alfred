package butler

import com.beust.jcommander.Parameter

class SshOptions {
    @Parameter(description = "Environment VM is in")
    var environment: String? = null

    @Parameter(names = arrayOf("--username", "-u"), description = "Your username for prod")
    var username: String? = null

    @Parameter(names = arrayOf("-vm"), description = "VM to SSH to", required = true)
    var vm: String? = null
}

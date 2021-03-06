package butler

import com.beust.jcommander.Parameter

class SshOptions {
    @Parameter(description = "Environment VM is in")
    var environment: String? = null

    @Parameter(names = arrayOf("--username", "-u"), description = "Username for remote server, if required")
    var username: String? = null

    @Parameter(names = arrayOf("-vm"), description = "VM to SSH to", required = true)
    var vm: String? = null
}

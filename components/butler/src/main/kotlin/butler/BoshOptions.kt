package butler

import com.beust.jcommander.Parameter

class BoshOptions {
    @Parameter(description = "Environment bosh director is in")
    var environment: String? = null

    @Parameter(names = arrayOf("--username", "-u"), description = "Username for remote server, if required")
    var username: String? = null

    @Parameter(names = arrayOf("-c", "--command"), description = "Bosh command to run", required = true)
    var command: String? = null
}

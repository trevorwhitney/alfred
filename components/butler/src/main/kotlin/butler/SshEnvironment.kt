package butler

interface SshEnvironment {
    val name: String
    val nickname: String

    fun ssh(vm: String, username: String?)
}

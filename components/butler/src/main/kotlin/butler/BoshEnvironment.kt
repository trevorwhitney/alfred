package butler

interface BoshEnvironment {
    val name: String
    val nickname: String

    fun bosh(boshCommand: String, deployment: String?, username: String?)
}

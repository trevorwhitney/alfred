package butler

data class EnvironmentInfo(
    val name: String,
    val nickname: String,
    var supportsSsh: Boolean = false,
    var supportsBosh: Boolean = false
)

fun buildEnvironmentInfo(name: String, nickname: String): EnvironmentInfo {
    return EnvironmentInfo(
        name = name,
        nickname = nickname
    )
}

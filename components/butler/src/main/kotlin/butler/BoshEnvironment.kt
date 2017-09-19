package butler

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class BoshEnvironment(
    val name: String,
    val nickname: String
)

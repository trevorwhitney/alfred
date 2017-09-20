package butler

interface Task {
    val name: String

    fun run(): Unit
}

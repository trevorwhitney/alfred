package alfred

import butler.Butler

fun main(args: Array<String>) {
    val alfred = Butler("Alfred")
    alfred.exec(args)
}

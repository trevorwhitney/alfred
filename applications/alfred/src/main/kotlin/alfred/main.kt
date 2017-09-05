package alfred

import butler.Butler

data class BoshConfig(
        val directorAddress: String, //$(bbl director-address)
        val directorCaCert: String, //$(bbl director-ca-cert)
        val directorUsername: String, //$(bbl director-username)
        val directorPassword: String, //$(bbl director-password)
        val gatewayUser: String, //jumpbox
        val gatewayHost: String, //$(bbl director-address | sed -e "s/^https:\/\///" -e "s/:25555$//")
        val gatewayPrivateKey: String //bbl ssh-key > /tmp/ssh.pem
)

fun main(args: Array<String>) {
    val alfred = Butler("Alfred")
    alfred.exec(args)
}

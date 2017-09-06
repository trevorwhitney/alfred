package bosh

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

fun loadDirectorConfig(path: String): DirectorConfig {
    val objectMapper = ObjectMapper().registerModule(KotlinModule())
    return objectMapper.readValue<DirectorConfig>(File(path).inputStream())
}

@JsonDeserialize(using = DirectorConfigDeserializer::class)
data class DirectorConfig(
    val directorAddress: String, //$(bbl director-address)
    val directorCaCert: String, //$(bbl director-ca-cert)
    val directorUsername: String, //$(bbl director-username)
    val directorPassword: String, //$(bbl director-password)
    val gatewayUser: String, //jumpbox
    val gatewayHost: String, //$(bbl director-address | sed -e "s/^https:\/\///" -e "s/:25555$//")
    val gatewayPrivateKey: String //bbl ssh-key > /tmp/ssh.pem
)

class DirectorConfigDeserializer : StdDeserializer<DirectorConfig>(DirectorConfig::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DirectorConfig {
        val objectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

        val node = p.codec.readTree<JsonNode>(p)
        val boshConfig = node.get("bosh")

        val directorAddress = boshConfig.get("directorAddress").asText()
        val directorCaCert = boshConfig.get("directorSSLCA").asText()
        val directorUsername = boshConfig.get("directorUsername").asText()
        val directorPassword = boshConfig.get("directorPassword").asText()

        val boshVariables = objectMapper.readValue<BoshVariables>(boshConfig.get("variables").asText())

        val gatewayHost = directorAddress.replace(Regex("^https://"), "").replace(Regex(":25555$"), "")

        return DirectorConfig(
            directorAddress = directorAddress,
            directorCaCert = directorCaCert,
            directorUsername = directorUsername,
            directorPassword = directorPassword,
            gatewayUser = "jumpbox",
            gatewayHost = gatewayHost,
            gatewayPrivateKey = boshVariables.privateKey
        )
    }
}

package bosh

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = BoshVariablesDeserializer::class)
data class BoshVariables(
    val privateKey: String
)

class BoshVariablesDeserializer : StdDeserializer<BoshVariables>(BoshVariables::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BoshVariables {
        val node = p.codec.readTree<JsonNode>(p)

        return BoshVariables(
            privateKey = node.get("jumpbox_ssh").get("private_key").asText()
        )
    }
}

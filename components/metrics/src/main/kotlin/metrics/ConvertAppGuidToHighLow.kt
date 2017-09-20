package metrics

import butler.IncorrectUsageException
import butler.Task
import com.beust.jcommander.Parameter
import java.math.BigInteger

class ConvertAppGuidToHighLow : Task {
    override val name = "high-low"

    @Parameter(description = "guid", required = true)
    var guid: String? = null

    override fun run() {
        val guidToConvert = guid

        if (guidToConvert === null) {
            throw IncorrectUsageException("No guid provided")
        }

        val guidString = guidToConvert.replace("-", "")
        val guidHigh = BigInteger(guidString.slice(0..15), 16)
        val guidLow = BigInteger(guidString.slice(16..31), 16)

        println("Parsed guid $guidToConvert to high and low values")
        println("High:\t$guidHigh")
        println("Low:\t$guidLow")
    }
}

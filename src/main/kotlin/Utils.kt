import java.nio.charset.Charset

class Utils {
    companion object {
        fun readResource(name: String): String {
            return Utils::class.java.classLoader.getResource(name).readText(Charset.defaultCharset())
        }
    }
}
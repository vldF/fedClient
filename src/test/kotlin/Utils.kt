import kotlin.random.Random

const val server = "130.61.203.95"

private val random by lazy { Random }
private val alphabet = ('A'..'Z').map { it } + ('a'..'z').map { it }

fun generateRandomString(): String {
    return (1..32).map { alphabet[random.nextInt(0, alphabet.size-1)] }.joinToString(separator = "")
}

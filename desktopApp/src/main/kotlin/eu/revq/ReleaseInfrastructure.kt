package eu.revq

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

fun interface HttpTextClient {
    suspend fun get(url: String): String
}

class CodebergReleaseSource(
    private val repository: String = "melokki/revq",
    private val httpGet: HttpTextClient = HttpTextClient(::httpGetText),
) : ReleaseSource {
    constructor(httpGet: HttpTextClient) : this("melokki/revq", httpGet)

    override suspend fun loadReleases(): List<ReleaseCandidate> {
        val payload = httpGet.get("https://codeberg.org/api/v1/repos/$repository/releases?limit=20")
        val releases = JsonParser(payload).parse() as? List<*> ?: error("Release response is not a list.")
        return releases.mapNotNull { raw ->
            val item = raw as? Map<*, *> ?: return@mapNotNull null
            val tag = item.string("tag_name") ?: return@mapNotNull null
            val assets = (item["assets"] as? List<*>).orEmpty().mapNotNull { rawAsset ->
                val asset = rawAsset as? Map<*, *> ?: return@mapNotNull null
                ReleaseAsset(
                    name = asset.string("name") ?: return@mapNotNull null,
                    downloadUrl = asset.string("browser_download_url") ?: return@mapNotNull null,
                    sizeBytes = (asset["size"] as? Number)?.toLong(),
                )
            }
            ReleaseCandidate(
                tag = tag,
                title = item.string("name")?.ifBlank { tag } ?: tag,
                notes = item.string("body").orEmpty(),
                publishedAt = item.string("published_at")?.let { runCatching { Instant.parse(it) }.getOrNull() },
                assets = assets,
                draft = item["draft"] as? Boolean ?: false,
                prerelease = item["prerelease"] as? Boolean ?: false,
            )
        }
    }
}

class HttpUpdateDownloadGateway(
    private val client: HttpClient = releaseHttpClient(),
) : UpdateDownloadGateway {
    @Volatile
    private var cancelled = false

    override suspend fun download(
        asset: ReleaseAsset,
        destination: Path,
        onProgress: (Long, Long?) -> Unit,
    ): Path {
        cancelled = false
        val request = releaseRequest(asset.downloadUrl)
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        require(response.statusCode() in 200..299) { "Update download returned HTTP ${response.statusCode()}." }
        val total = response.headers().firstValueAsLong("Content-Length").orElse(-1L).takeIf { it >= 0 }
        Files.createDirectories(destination.parent)
        response.body().use { input ->
            Files.newOutputStream(destination).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L
                while (true) {
                    check(!cancelled) { "Update download cancelled." }
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    downloaded += count
                    onProgress(downloaded, total)
                }
            }
        }
        return destination
    }

    override suspend fun downloadText(asset: ReleaseAsset): String = httpGetText(asset.downloadUrl)

    override fun cancel() {
        cancelled = true
    }
}

private fun Map<*, *>.string(key: String): String? = this[key] as? String

private fun releaseHttpClient(): HttpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

private fun releaseRequest(url: String): HttpRequest = HttpRequest.newBuilder(URI.create(url))
    .timeout(Duration.ofSeconds(30))
    .header("Accept", "application/json")
    .header("User-Agent", "RevQ-Updater")
    .GET()
    .build()

private fun httpGetText(url: String): String {
    val response = releaseHttpClient().send(releaseRequest(url), HttpResponse.BodyHandlers.ofString())
    require(response.statusCode() in 200..299) { "Release source returned HTTP ${response.statusCode()}." }
    return response.body()
}

private class JsonParser(
    private val input: String,
) {
    private var index = 0

    fun parse(): Any? {
        val value = value()
        whitespace()
        require(index == input.length) { "Unexpected JSON content at $index." }
        return value
    }

    private fun value(): Any? {
        whitespace()
        require(index < input.length) { "Unexpected end of JSON." }
        return when (input[index]) {
            '{' -> objectValue()
            '[' -> arrayValue()
            '"' -> stringValue()
            't' -> literal("true", true)
            'f' -> literal("false", false)
            'n' -> literal("null", null)
            else -> numberValue()
        }
    }

    private fun objectValue(): Map<String, Any?> {
        index += 1
        whitespace()
        val result = linkedMapOf<String, Any?>()
        if (take('}')) return result
        while (true) {
            whitespace()
            val key = stringValue()
            whitespace()
            require(take(':')) { "Expected ':' at $index." }
            result[key] = value()
            whitespace()
            if (take('}')) return result
            require(take(',')) { "Expected ',' at $index." }
        }
    }

    private fun arrayValue(): List<Any?> {
        index += 1
        whitespace()
        val result = mutableListOf<Any?>()
        if (take(']')) return result
        while (true) {
            result += value()
            whitespace()
            if (take(']')) return result
            require(take(',')) { "Expected ',' at $index." }
        }
    }

    private fun stringValue(): String {
        require(take('"')) { "Expected string at $index." }
        val result = StringBuilder()
        while (index < input.length) {
            val character = input[index++]
            when (character) {
                '"' -> return result.toString()
                '\\' -> {
                    require(index < input.length) { "Incomplete JSON escape." }
                    when (val escaped = input[index++]) {
                        '"', '\\', '/' -> result.append(escaped)
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        't' -> result.append('\t')
                        'u' -> {
                            val hex = input.substring(index, index + 4)
                            result.append(hex.toInt(16).toChar())
                            index += 4
                        }
                        else -> error("Invalid JSON escape: $escaped")
                    }
                }
                else -> result.append(character)
            }
        }
        error("Unterminated JSON string.")
    }

    private fun numberValue(): Number {
        val start = index
        while (index < input.length && input[index] in "-+0123456789.eE") index += 1
        val raw = input.substring(start, index)
        return raw.toLongOrNull() ?: raw.toDoubleOrNull() ?: error("Invalid JSON number: $raw")
    }

    private fun literal(text: String, value: Any?): Any? {
        require(input.startsWith(text, index)) { "Expected $text at $index." }
        index += text.length
        return value
    }

    private fun whitespace() {
        while (index < input.length && input[index].isWhitespace()) index += 1
    }

    private fun take(character: Char): Boolean {
        if (index >= input.length || input[index] != character) return false
        index += 1
        return true
    }
}

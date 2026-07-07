package eu.revq

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
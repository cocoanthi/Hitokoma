package jp.pinolab.hitokoma

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
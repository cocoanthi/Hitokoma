package jp.pinolab.hitokoma.core.file

expect class LocalImageStorage {
    /**
     * 画像のバイト配列を受け取り、内部ストレージに保存して絶対パスを返す
     */
    suspend fun saveImage(bytes: ByteArray, fileName: String): String
}
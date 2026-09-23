package jp.pinolab.hitokoma.core.file

expect class LocalImageStorage {
    /**
     * 画像のバイト配列を受け取り、内部ストレージに保存して絶対パスを返す
     */
    suspend fun saveImage(bytes: ByteArray, fileName: String): String

    /**
     * 保存済みの画像を削除する（ファイルが存在しない場合は何もしない）
     */
    suspend fun deleteImage(path: String)
}
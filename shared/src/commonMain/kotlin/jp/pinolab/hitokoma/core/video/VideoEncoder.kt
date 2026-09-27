package jp.pinolab.hitokoma.core.video

/**
 * ストーリー動画の1枚分（写真と、その上に重ねる文字）
 */
data class VideoSlide(
    val imagePath: String, // ローカルストレージ内の画像ファイルパス
    val dateLabel: String, // 例: "2026年9月24日"
    val comment: String    // 空の場合は表示しない
)

/**
 * スライドを縦型のスライドショー動画（MP4）に書き出す
 */
interface VideoEncoder {
    /**
     * 冒頭にタイトル、続いて slides を順に並べた動画を outputPath に書き出す。失敗時は例外を投げる
     */
    suspend fun encode(title: String, slides: List<VideoSlide>, outputPath: String)
}

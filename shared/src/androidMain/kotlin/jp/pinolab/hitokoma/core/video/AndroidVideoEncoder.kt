package jp.pinolab.hitokoma.core.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 各スライドを 1080x1920 の画像に描き出し、Media3 Transformer で MP4（H.264）に書き出す
 */
class AndroidVideoEncoder(private val context: Context) : VideoEncoder {

    override suspend fun encode(title: String, slides: List<VideoSlide>, outputPath: String) {
        val framesDir = File(context.cacheDir, "story_frames")
        val output = File(outputPath)
        // 書き出し途中のファイルを「生成済み」と判定しないよう、完成してからリネームする
        val tempOutput = File("$outputPath.tmp")

        try {
            val frames = withContext(Dispatchers.IO) {
                framesDir.deleteRecursively()
                framesDir.mkdirs()
                output.parentFile?.mkdirs()
                tempOutput.delete()

                buildList {
                    add(renderTitle(title).saveTo(File(framesDir, "000.jpg")))
                    slides.forEachIndexed { index, slide ->
                        add(renderSlide(slide).saveTo(File(framesDir, "%03d.jpg".format(index + 1))))
                    }
                }
            }

            // Transformer はメインスレッド（Looper のあるスレッド）から操作する
            withContext(Dispatchers.Main) { export(frames, tempOutput) }

            withContext(Dispatchers.IO) {
                output.delete()
                if (!tempOutput.renameTo(output)) throw IOException("動画ファイルを保存できませんでした: $outputPath")
            }
        } finally {
            withContext(Dispatchers.IO) {
                framesDir.deleteRecursively()
                tempOutput.delete()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun export(frames: List<File>, output: File) = suspendCancellableCoroutine { continuation ->
        val items = frames.map { frame ->
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.fromFile(frame))
                .setMimeType(MimeTypes.IMAGE_JPEG)
                .setImageDurationMs(SLIDE_DURATION_MS)
                .build()
            EditedMediaItem.Builder(mediaItem)
                .setFrameRate(FRAME_RATE)
                .build()
        }
        val composition = Composition.Builder(EditedMediaItemSequence(items)).build()

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            // 既定では横向きにエンコードして回転情報を付けるため、回転情報を無視するアプリでも縦に見えるよう縦のまま書き出す
            .setPortraitEncodingEnabled(true)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    continuation.resume(Unit)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    continuation.resumeWithException(exportException)
                }
            })
            .build()

        transformer.start(composition, output.absolutePath)

        // キャンセルはどのスレッドから来るか分からないため、メインスレッドで Transformer を止める
        continuation.invokeOnCancellation {
            Handler(Looper.getMainLooper()).post { transformer.cancel() }
        }
    }

    /**
     * 冒頭のタイトル画面（例: 「2026年9月」）
     */
    private fun renderTitle(title: String): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BACKGROUND_COLOR)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 120f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 52f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, WIDTH / 2f, HEIGHT / 2f, titlePaint)
        canvas.drawText("今日の一枚をふりかえり", WIDTH / 2f, HEIGHT / 2f + 110f, subtitlePaint)
        return bitmap
    }

    /**
     * 写真を画面いっぱいに敷き、下部に日付とコメントを重ねたスライド
     */
    private fun renderSlide(slide: VideoSlide): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BACKGROUND_COLOR)

        // 写真（読み込めない場合は背景色のまま文字だけ表示する）
        decodePhoto(slide.imagePath)?.let { photo ->
            canvas.drawBitmap(photo, centerCropMatrix(photo.width, photo.height), Paint(Paint.FILTER_BITMAP_FLAG))
            photo.recycle()
        }

        // 文字を読みやすくするため、下半分に黒のグラデーションを敷く
        val gradientTop = HEIGHT * 0.5f
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, gradientTop, 0f, HEIGHT.toFloat(),
                Color.TRANSPARENT, Color.argb(200, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, gradientTop, WIDTH.toFloat(), HEIGHT.toFloat(), gradientPaint)

        // 下から順に積む: コメント → 日付
        val textWidth = WIDTH - TEXT_HORIZONTAL_PADDING * 2
        var bottom = HEIGHT - TEXT_BOTTOM_PADDING

        if (slide.comment.isNotBlank()) {
            val commentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 48f
            }
            val commentLayout = StaticLayout.Builder
                .obtain(slide.comment, 0, slide.comment.length, commentPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setMaxLines(COMMENT_MAX_LINES)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
            bottom -= commentLayout.height
            canvas.save()
            canvas.translate(TEXT_HORIZONTAL_PADDING.toFloat(), bottom.toFloat())
            commentLayout.draw(canvas)
            canvas.restore()
            bottom -= 24
        }

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(slide.dateLabel, TEXT_HORIZONTAL_PADDING.toFloat(), bottom - datePaint.descent(), datePaint)

        return bitmap
    }

    /**
     * 画面を覆うのに足りる解像度まで縮小して読み込む（メモリ節約のため）
     */
    private fun decodePhoto(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // 画面を覆うのに必要な縮小率。これを下回らない範囲で 2 の累乗ずつ縮小してデコードする
        val coverScale = maxOf(WIDTH.toFloat() / bounds.outWidth, HEIGHT.toFloat() / bounds.outHeight)
        var sampleSize = 1
        while (coverScale * sampleSize * 2 <= 1f) {
            sampleSize *= 2
        }
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
    }

    /**
     * 画像を縦横比を保ったまま画面いっぱいに拡大し、はみ出た部分を中央で切り落とす
     */
    private fun centerCropMatrix(width: Int, height: Int): Matrix {
        val scale = maxOf(WIDTH.toFloat() / width, HEIGHT.toFloat() / height)
        return Matrix().apply {
            setScale(scale, scale)
            postTranslate((WIDTH - width * scale) / 2f, (HEIGHT - height * scale) / 2f)
        }
    }

    private fun Bitmap.saveTo(file: File): File {
        file.outputStream().use { compress(Bitmap.CompressFormat.JPEG, 92, it) }
        recycle()
        return file
    }

    private companion object {
        // ストーリー向けの縦型 9:16
        const val WIDTH = 1080
        const val HEIGHT = 1920
        const val FRAME_RATE = 30
        const val SLIDE_DURATION_MS = 2_000L

        const val TEXT_HORIZONTAL_PADDING = 80
        const val TEXT_BOTTOM_PADDING = 220 // SNS のストーリー UI と重ならないよう下端から離す
        const val COMMENT_MAX_LINES = 6

        val BACKGROUND_COLOR = Color.rgb(28, 27, 31)
    }
}

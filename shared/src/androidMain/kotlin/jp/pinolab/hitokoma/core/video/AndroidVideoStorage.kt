package jp.pinolab.hitokoma.core.video

import android.content.Context
import kotlinx.datetime.LocalDate
import java.io.File

/**
 * filesDir/videos/yyyy-MM.mp4 に月ごとの動画を保存する
 */
class AndroidVideoStorage(context: Context) : VideoStorage {

    private val videosDir = File(context.filesDir, "videos")

    override fun videoPath(month: LocalDate): String =
        File(videosDir, "${month.year}-${month.monthNumber.toString().padStart(2, '0')}.mp4").absolutePath

    override fun existingMonths(): Set<LocalDate> =
        videosDir.listFiles().orEmpty()
            .mapNotNull { file ->
                val match = FILE_NAME.matchEntire(file.name) ?: return@mapNotNull null
                val (year, month) = match.destructured
                LocalDate(year.toInt(), month.toInt(), 1)
            }
            .toSet()

    private companion object {
        val FILE_NAME = Regex("""(\d{4})-(\d{2})\.mp4""")
    }
}

package jp.pinolab.hitokoma.core.video

import kotlinx.datetime.LocalDate

/**
 * 月ごとのストーリー動画の保存先
 */
interface VideoStorage {
    /**
     * 指定した月の動画ファイルの絶対パス（ファイルが存在するとは限らない）
     */
    fun videoPath(month: LocalDate): String

    /**
     * 動画が保存済みの月（各月の1日）
     */
    fun existingMonths(): Set<LocalDate>
}

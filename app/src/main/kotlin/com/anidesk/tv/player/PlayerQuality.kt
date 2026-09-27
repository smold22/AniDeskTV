package com.anidesk.tv.player

/**
 * Нормализация качества и единая модель выбора (балансер):
 * озвучка -> источник -> серия -> качество.
 */
object PlayerQuality {

    private val QUALITY_ORDER = listOf(360, 480, 720, 1080, 1440, 2160)

    /** Доступные качества (Int) из [links], отсортированные по убыванию. */
    fun available(links: Map<String, String>): List<Int> =
        links.keys.mapNotNull { it.toIntOrNull() }.sortedDescending()

    /** Варианты качества для UI: «Авто» + доступные. */
    fun labels(links: Map<String, String>): List<Pair<Int, String>> =
        listOf(0 to "Авто") + available(links).map { it to "${it}p" }

    /**
     * Выбор URL по запрошенному качеству с фолбэком:
     * авто/недоступное -> ближайшее доступное не выше запрошенного (или максимальное).
     */
    fun pickUrl(links: Map<String, String>, quality: Int): String? {
        if (quality > 0) {
            links[quality.toString()]?.let { return it }
            val avail = available(links)
            val nearest = avail.firstOrNull { it <= quality } ?: avail.maxOrNull()
            nearest?.let { links[it.toString()]?.let { url -> return url } }
            return links.values.firstOrNull()
        }
        available(links).maxOrNull()?.let { links[it.toString()]?.let { url -> return url } }
        return links.values.firstOrNull()
    }

    /** Ближайшая метка для отображения текущего выбранного качества. */
    fun labelOf(quality: Int): String =
        if (quality == 0) "Авто" else "${quality}p"
}

/** Единая модель выбора (балансер): озвучка -> источник -> серия -> качество. */
data class PlayerSelection(
    val releaseId: Int = 0,
    val dubberId: Int = 0,
    val sourceId: Int = 0,
    val sourceName: String = "",
    val episodePosition: Int = 0,
    val quality: Int = 0,
)
package com.anidesk.tv.core.model.release

/**
 * Статус пользовательского списка Anixart — то, что в мобильном клиенте называется «закладкой».
 * Тайтл может находиться только в одном списке, поэтому статус всегда один.
 *
 * [type] — значение, которым Anixart оперирует в `profile_list_status` и в
 * `/profile/list/{add,delete}/{type}/{releaseId}`. [NONE] означает «без закладки»: тайтл
 * нужно предварительно убрать из текущего списка.
 */
enum class BookmarkStatus(val type: Int) {
    NONE(0),
    WATCHING(1),
    PLANNED(2),
    COMPLETED(3),
    ON_HOLD(4),
    DROPPED(5),
    ;

    companion object {
        /** Статус по значению из API; неизвестное или отсутствующее значение — [NONE]. */
        fun fromType(type: Int?): BookmarkStatus =
            entries.firstOrNull { it.type == type } ?: NONE
    }
}

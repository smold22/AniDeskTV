package com.anidesk.tv.feature.schedule

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.feature.details.IDetailsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<ScheduleState.State, ScheduleState.Event, ScheduleState.Effect>() {

    override fun createInitialState(): ScheduleState.State =
        ScheduleState.State(selectedDay = LocalDate.now().dayOfWeek.value - 1)

    init {
        loadSchedule()
    }

    override fun onEvent(event: ScheduleState.Event) {
        when (event) {
            is ScheduleState.Event.DaySelected -> setState { copy(selectedDay = event.day) }

            is ScheduleState.Event.AnimeSelected -> nav.navigate(
                detailsNavigator.getDetailsDest(
                    releaseId = event.release.id,
                    posterUrl = event.release.posterUrl,
                    title = event.release.titleRu.ifBlank { event.release.titleOriginal },
                ),
            )

            is ScheduleState.Event.Retry -> {
                setState { copy(isLoading = true, error = null) }
                loadSchedule()
            }
        }
    }

    override fun onFailed(exception: Throwable) {
        setState { copy(isLoading = false, error = exception.message) }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val schedule = apiProvider.get().schedule()
            setState {
                copy(
                    isLoading = false,
                    days = listOf(
                        schedule.monday,
                        schedule.tuesday,
                        schedule.wednesday,
                        schedule.thursday,
                        schedule.friday,
                        schedule.saturday,
                        schedule.sunday,
                    ).map { day -> day.map { it.toAnimeRelease() } },
                )
            }
        }
    }

    private fun Release.toAnimeRelease(): AnimeRelease =
        AnimeRelease(
            id = id,
            titleRu = titleRu,
            titleOriginal = titleOriginal,
            posterUrl = image,
            year = year.toIntOrNull(),
            rating = grade,
            genres = genres.split(',').map { it.trim() }.filter { it.isNotEmpty() },
            category = category?.name.orEmpty(),
            episodesTotal = episodesTotal,
            isViewed = isViewed,
        )
}
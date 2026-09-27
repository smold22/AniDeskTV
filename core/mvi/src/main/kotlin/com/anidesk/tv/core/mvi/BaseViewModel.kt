package com.anidesk.tv.core.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * База MVI-экрана: держит [state], раздаёт одноразовые [effect] и дефолтно обрабатывает
 * ошибки в [onError] — наследнику остаётся описать начальное состояние и реакцию на события.
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect> : CoroutineViewModel() {

    protected abstract fun createInitialState(): S

    private val _state by lazy { MutableStateFlow(createInitialState()) }
    val state: StateFlow<S> by lazy { _state.asStateFlow() }

    val currentState: S get() = _state.value

    protected fun setState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    private val _effect = MutableSharedFlow<F>()
    val effect: SharedFlow<F> = _effect.asSharedFlow()

    protected fun setEffect(effect: F) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    fun setEvent(event: E) = onEvent(event)
    protected abstract fun onEvent(event: E)

    override fun onError(exception: Throwable) = onFailed(exception)
    protected open fun onFailed(exception: Throwable) {}
}
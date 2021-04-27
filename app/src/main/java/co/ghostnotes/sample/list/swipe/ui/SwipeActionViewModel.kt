package co.ghostnotes.sample.list.swipe.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ghostnotes.sample.list.swipe.SwipeAction
import co.ghostnotes.sample.list.swipe.SwipeActionData
import co.ghostnotes.sample.list.swipe.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SwipeActionViewModel @Inject constructor() : ViewModel() {

    @DefaultDispatcher
    @Inject
    lateinit var defaultDispatcher: CoroutineDispatcher

    private val _test = MutableLiveData<Boolean>()
    val test: LiveData<Boolean> = _test

    private val _swipeActionData = MutableSharedFlow<SwipeActionData>()
    val swipeActionData: SharedFlow<SwipeActionData> = _swipeActionData

    private val _undoDeletingSwipeAction = MutableSharedFlow<Int>()
    val undoDeletingSwipeAction: SharedFlow<Int> = _undoDeletingSwipeAction

    fun showSnackbar(swipeAction: SwipeAction, position: Int) {
        viewModelScope.launch(defaultDispatcher) {
            _test.postValue(true)

            try {
                _swipeActionData.emit(SwipeActionData(swipeAction, position))
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                _test.postValue(false)
            }
        }
    }

    fun undoDeletingSwipeAction(itemPosition: Int) {
        viewModelScope.launch(defaultDispatcher) {
            _undoDeletingSwipeAction.emit(itemPosition)
        }
    }
}

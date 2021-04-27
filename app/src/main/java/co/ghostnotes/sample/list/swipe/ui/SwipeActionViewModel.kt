package co.ghostnotes.sample.list.swipe.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ghostnotes.sample.list.swipe.SwipeAction
import co.ghostnotes.sample.list.swipe.SwipeActionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SwipeActionViewModel @Inject constructor() : ViewModel() {

    private val _test = MutableLiveData<Boolean>()
    val test: LiveData<Boolean> = _test

    private val _swipeActionData = MutableSharedFlow<SwipeActionData>()
    val swipeActionData: SharedFlow<SwipeActionData> = _swipeActionData

    suspend fun showSnackbar(swipeAction: SwipeAction, position: Int) {
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

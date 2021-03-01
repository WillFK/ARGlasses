package fk.home.arglasses.ui.glasses

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlassesViewModel : ViewModel() {

    val state = MutableLiveData<GlassesState>()
    val messages = MutableLiveData<GlassesMessageWrapper>()

    private val reducer = GlassesReducer(
        {
            state.postValue(it)
        },
        {
            messages.postValue(GlassesMessageWrapper(it))
        }
    )
    init {
        state.postValue(GlassesState())
    }

    fun processAction(action: GlassesAction) {
        reducer.reduce(state.value!!, action)
    }
}

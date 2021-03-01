package fk.home.arglasses.ui.glasses

import com.google.ar.sceneform.math.Vector3

/**
 * Not the best approach with the lambdas,
 * Trying to compensate for the lack of coroutines or RxJava in this project.
 */
class GlassesReducer(
    private val updateState: (GlassesState) -> Unit,
    private val postMessage: (GlassesMessage) -> Unit
) {

    fun reduce(
        state: GlassesState,
        action: GlassesAction
    ) {
        when (action) {
            is GlassesAction.CloseControlPanel -> {
                state.copy(hudConfig = GlassesHudConfig.BUTTONS).let(updateState)
            }
            is GlassesAction.OpenCamera -> {
                postMessage(GlassesMessage.CaptureImage)
            }
            is GlassesAction.OpenPositionControlPanel -> {
                state.copy(hudConfig = GlassesHudConfig.POSITION).let(updateState)
            }
            is GlassesAction.OpenSizeControlPanel -> {
                state.copy(hudConfig = GlassesHudConfig.SIZE).let(updateState)
            }
            is GlassesAction.UpdatePosition -> {
                state.copy(
                    valuesPos = Vector3(action.position.x, action.position.y, action.position.z)
                ).let(updateState)
            }
            is GlassesAction.UpdateSize -> {
                state.copy(
                    valuesSize = Vector3(action.size.x, action.size.y, action.size.z)
                ).let(updateState)
            }
        }
    }
}
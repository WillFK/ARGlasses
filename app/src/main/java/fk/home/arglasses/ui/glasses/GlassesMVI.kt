package fk.home.arglasses.ui.glasses

import com.google.ar.sceneform.math.Vector3

data class GlassesState(
    val hudConfig: GlassesHudConfig = GlassesHudConfig.BUTTONS,
    val valuesPos: Vector3 = Vector3(.5f, .5f, .5f),
    val valuesSize: Vector3 = Vector3(.5f, .5f, .5f)
)

sealed class GlassesMessage {

    object CaptureImage : GlassesMessage()
}

class GlassesMessageWrapper(private var message: GlassesMessage?) {

    fun popMessage(): GlassesMessage? {
        return message?.let {
            this.message = null
            it
        }
    }
}

sealed class GlassesAction {

    object CloseControlPanel : GlassesAction()

    object OpenPositionControlPanel : GlassesAction()

    object OpenSizeControlPanel : GlassesAction()

    object OpenCamera : GlassesAction()

    data class UpdateSize(val size: Vector3) : GlassesAction()

    data class UpdatePosition(val position: Vector3) : GlassesAction()
}

enum class GlassesHudConfig {

    BUTTONS,
    SIZE,
    POSITION
}
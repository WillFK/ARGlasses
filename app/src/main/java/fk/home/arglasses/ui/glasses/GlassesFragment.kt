package fk.home.arglasses.ui.glasses

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import fk.home.arglasses.R
import fk.home.arglasses.ar.ModelLoader
import fk.home.arglasses.io.persistBitmap
import kotlinx.android.synthetic.main.fragment_glasses.*
import java.io.IOException
import java.util.*


class GlassesFragment : Fragment() {

    private var viewModel: GlassesViewModel? = null

    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()
    private lateinit var modelLoader: ModelLoader
    private lateinit var glassesRenderable: ModelRenderable
    //private lateinit var videoRecorder: VideoRecorder

    private var valuesPos: Vector3? = null
    private var valuesSize: Vector3? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_glasses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadModels()
        initArFaceScene()
        setupListeners()
        viewModel = ViewModelProvider(this).get(GlassesViewModel::class.java).apply {

            this.state.observe(viewLifecycleOwner, Observer {
                renderState(it)
            })
            this.messages.observe(viewLifecycleOwner, Observer {
                it.popMessage()?.let(this@GlassesFragment::processMessage)
            })
        }
    }

    private fun setupListeners() {
        glasses_button_camera.setOnClickListener {
            viewModel?.processAction(GlassesAction.OpenCamera)
        }
        glasses_button_position.setOnClickListener {
            viewModel?.processAction(GlassesAction.OpenPositionControlPanel)
        }
        glasses_button_size.setOnClickListener {
            viewModel?.processAction(GlassesAction.OpenSizeControlPanel)
        }
        glasses_position_control_panel_close.setOnClickListener {
            viewModel?.processAction(GlassesAction.CloseControlPanel)
        }
        glasses_size_control_panel.setOnClickListener {
            viewModel?.processAction(GlassesAction.CloseControlPanel)
        }

        glasses_control_panel_x_slider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            Log.d("FKZ", "${glasses_control_panel_x_slider.value}  -  $value")
            if (fromUser) {
                onPositionChanged()
            }
        })
        glasses_control_panel_y_slider.addOnChangeListener(Slider.OnChangeListener { _, _, fromUser ->
            if (fromUser) {
                onPositionChanged()
            }
        })
        glasses_control_panel_z_slider.addOnChangeListener(Slider.OnChangeListener { _, _, fromUser ->
            if (fromUser) {
                onPositionChanged()
            }
        })

        glasses_control_panel_width_slider.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                onSizeChanged()
            }
        }

        glasses_control_panel_height_slider.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                onSizeChanged()
            }
        }

        glasses_control_panel_depth_slider.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                onSizeChanged()
            }
        }
    }

    private fun onPositionChanged() {
        viewModel?.processAction(
            GlassesAction.UpdatePosition(
                Vector3(
                    glasses_control_panel_x_slider.value,
                    glasses_control_panel_y_slider.value,
                    glasses_control_panel_z_slider.value
                )
            )
        )
    }

    private fun onSizeChanged() {
        viewModel?.processAction(
            GlassesAction.UpdateSize(
                Vector3(
                    glasses_control_panel_width_slider.value,
                    glasses_control_panel_height_slider.value,
                    glasses_control_panel_depth_slider.value
                )
            )
        )
    }

    private fun loadModels() {
        modelLoader = ModelLoader(requireActivity())

        modelLoader.loadModel(GLASSES_MODEL, R.raw.glasses,
            { _, modelRenderable ->
                glassesRenderable = modelRenderable!!
                glassesRenderable.isShadowReceiver = false
                glassesRenderable.isShadowCaster = false
                null

            },
            { _, _ ->
                Toast.makeText(
                    requireContext(), "Can't load Mario Hat",
                    Toast.LENGTH_LONG
                ).show()
                null
            })
    }

    private fun initArFaceScene() {
        val sceneView = childFragmentManager.findFragmentById(R.id.glasses_ar_fragment)
            .let { it as GlassesArFragment }
            .arSceneView

        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val scene = sceneView.scene

        scene.addOnUpdateListener { _ ->
            val faceList =
                sceneView.session!!.getAllTrackables(
                    AugmentedFace::class.java
                )
            // Make new AugmentedFaceNodes for any new faces.
            for (face in faceList) {
                if (!faceNodeMap.containsKey(face)) {

                    val faceNode = AugmentedFaceNode(face)
                    faceNode.setParent(scene)
                    faceNode.faceRegionsRenderable = glassesRenderable

                    faceNode.localScale = Vector3(0.04f, 0.04f, 0.04f)

                    faceNodeMap[face] = faceNode
                }
            }

            // access skeleton node
            faceNodeMap.values.firstOrNull()?.children?.get(1)?.let { node ->
                //it.localScale = Vector3(it.localScale.x + 0.01f, it.localScale.y + 0.01f, it.localScale.z + 0.01f)
                valuesPos?.let {

                    node.localPosition = Vector3(
                        (it.x - 0.5f) * MAGIC_POS_NUMBER,
                        (it.y - 0.5f) * MAGIC_POS_NUMBER,
                        (it.z - 0.5f) * MAGIC_POS_NUMBER
                    )
                }

                valuesSize?.let {

                    node.localScale = Vector3(
                        it.x + MAGIC_SIZE_NUMBER,
                        it.y + MAGIC_SIZE_NUMBER,
                        it.z + MAGIC_SIZE_NUMBER
                    )
                }
            }

            val iter: MutableIterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> =
                faceNodeMap.entries.iterator()
            while (iter.hasNext()) {
                val entry =
                    iter.next()
                val face = entry.key
                if (face.trackingState == TrackingState.STOPPED) {
                    val faceNode = entry.value
                    faceNode.setParent(null)
                    iter.remove()
                }
            }
        }
    }

    private fun renderState(state: GlassesState) {
        renderButtons(state)
        renderPositionControlPanel(state)
        renderSizeControlPanel(state)
        updateRenderVectors(state)
    }

    private fun renderButtons(state: GlassesState) {
        glasses_button_group.isVisible = state.hudConfig == GlassesHudConfig.BUTTONS
    }

    private fun renderPositionControlPanel(state: GlassesState) {
        glasses_position_control_panel.isVisible = state.hudConfig == GlassesHudConfig.POSITION
        glasses_control_panel_x_slider.value = state.valuesPos.x
        glasses_control_panel_y_slider.value = state.valuesPos.y
        glasses_control_panel_z_slider.value = state.valuesPos.z
    }

    private fun renderSizeControlPanel(state: GlassesState) {
        glasses_size_control_panel.isVisible = state.hudConfig == GlassesHudConfig.SIZE
        glasses_control_panel_width_slider.value = state.valuesSize.x
        glasses_control_panel_height_slider.value = state.valuesSize.y
        glasses_control_panel_depth_slider.value = state.valuesSize.z
    }

    private fun updateRenderVectors(state: GlassesState) {
        valuesPos = state.valuesPos
        valuesSize = state.valuesSize
    }

    private fun processMessage(message: GlassesMessage) {
        when (message) {
            is GlassesMessage.CaptureImage -> {
                captureImage()
            }
        }
    }

    private fun captureImage() {

        val bitmap = Bitmap.createBitmap(
            view!!.width, view!!.height,
            Bitmap.Config.ARGB_8888
        )
        val sceneView = childFragmentManager.findFragmentById(R.id.glasses_ar_fragment)
            .let { it as GlassesArFragment }
            .arSceneView

        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        PixelCopy.request(sceneView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    shareImage(persistBitmap(requireContext(), bitmap))
                } catch (e: IOException) {
                    val toast: Toast = Toast.makeText(
                        requireActivity(), e.toString(),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return@request
                }
            } else {
                Toast.makeText(requireContext(), "Couldn't persist File", Toast.LENGTH_LONG).show()
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun shareImage(uri: Uri) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(share)
    }

    private companion object {
        const val GLASSES_MODEL = 1002
        const val MAGIC_POS_NUMBER = 5
        const val MAGIC_SIZE_NUMBER = .5f
    }
}

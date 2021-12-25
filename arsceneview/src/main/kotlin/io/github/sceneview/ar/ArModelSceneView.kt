package io.github.sceneview.ar

import android.content.Context
import android.util.AttributeSet
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode

/**
 * A SurfaceView that integrates with ARCore and renders a scene.
 */
open class ArModelSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ArSceneView(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    val modelNode = ArModelNode()

    var modelLocation: String? = null
    set(value) {
        if(field != value) {
            field = value
        }
    }

    /**
     * TODO: Doc
     *
     * @see io.github.sceneview.ar.node.ArModelNode.modelFile
     */
    var modelLocation: String?
        get() = modelNode.modelFile
        set(value) {
            modelNode.modelFile = value
        }

    /**
     * TODO: Doc
     *
     * @see io.github.sceneview.ar.node.ArModelNode.placementMode
     */
    var placementMode: PlacementMode
        get() = modelNode.placementMode
        set(value) {
            modelNode.placementMode = value
        }

    init {
    }
}
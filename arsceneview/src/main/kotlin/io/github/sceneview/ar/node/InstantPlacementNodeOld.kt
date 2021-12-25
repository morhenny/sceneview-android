package io.github.sceneview.ar.node

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.RenderableInstance
import io.github.sceneview.Position
import io.github.sceneview.Rotation
import io.github.sceneview.Scale
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.SceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.ar.arcore.depthEnabled
import io.github.sceneview.ar.arcore.isTracking
import io.github.sceneview.node.NodeParent

/**
 * ### Depth aligned node
 *
 * This [Node] follows the actual ARCore detected depth orientation and position at the provided
 * relative X, Y location in the [ArSceneView]
 *
 * You can:
 * - [anchor] this node at any time to make it fixed at the actual position and rotation.
 * This node will stop following the hitPostion to stay in place.
 * - [createAnchor] in order to extract a fixed/anchored copy of the actual.
 * This node will continue following the [com.google.ar.core.Camera]
 */
open class InstantPlacementNodeOld(
    position: Position = defaultPosition,
    rotation: Rotation = defaultRotation,
    scales: Scale = defaultScales,
    parent: NodeParent? = null
) : ArNode(
    position = position,
    rotation = rotation,
    scales = scales,
    parent = parent
) {

    var lastValidHitResult: HitResult? = null

    var isTracking: Boolean = false
        internal set(value) {
            if (field != value) {
                field = value
                isVisible = value
                onTrackingChanged?.invoke(this, value)
            }
        }

    var onArFrameHitResult: ((node: InstantPlacementNodeOld, hitResult: HitResult?, isTracking: Boolean) -> Unit)? =
        null
    var onTrackingChanged: ((node: InstantPlacementNodeOld, isTracking: Boolean) -> Unit)? = null

    init {
        isVisible = false
    }

    constructor(
        context: Context,
        modelGlbFileLocation: String,
        coroutineScope: LifecycleCoroutineScope? = null,
        onModelLoaded: ((instance: RenderableInstance) -> Unit)? = null,
        onError: ((error: Exception) -> Unit)? = null,
        parent: NodeParent? = null,
        position: Position = defaultPosition,
        rotation: Rotation = defaultRotation,
        scales: Scale = defaultScales,
    ) : this(position, rotation, scales, parent) {
        loadModel(context, modelGlbFileLocation, coroutineScope, onModelLoaded, onError)
    }

    override fun onAttachToScene(sceneView: SceneView) {
        super.onAttachToScene(sceneView)

        (sceneView as? ArSceneView)?.configureSession { config ->
            config.depthEnabled = true
        }
    }

    override fun onArFrame(frame: ArFrame) {
        super.onArFrame(frame)

        if (anchor == null) {
            val hitResult = hitTest(frame)
            onArFrameHitResult(hitResult, hitResult?.trackable?.isTracking == true)
        }
        isTracking = pose != null || lastValidHitResult?.isTracking == true
    }

    open fun onArFrameHitResult(hitResult: HitResult?, isTracking: Boolean) {
        // Keep the last position when no more tracking result
        if (hitResult != null && isTracking) {
            lastValidHitResult = hitResult
            hitResult.hitPose?.let { hitPose ->
                pose = hitPose
            }
        }
        onArFrameHitResult?.invoke(this, hitResult, isTracking)
    }

    override fun createAnchor(): Anchor? {
        return super.createAnchor() ?: if (lastValidHitResult?.isTracking == true) {
            lastValidHitResult?.createAnchor()
        } else null
    }
}

package io.github.sceneview.ar.node

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.filament.utils.*
import com.google.ar.core.*
import com.google.ar.core.Config.PlaneFindingMode
import com.google.ar.sceneform.rendering.RenderableInstance
import io.github.sceneview.*
import io.github.sceneview.ar.ArSceneLifecycleObserver
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.*
import io.github.sceneview.node.ModelNode

open class ArModelNode(
    /**
     * ### The node camera/screen position
     *
     * - While there is no AR tracking information available, the node is following the camera moves
     * so it stays at this camera/screen relative position [com.google.ar.sceneform.Camera] node is
     * considered as the parent)
     * - ARCore will try to find the real world position of this screen position and the node
     * [worldPosition] will be updated so.
     *
     * The Z value is only used when no surface is actually detected or when instant placement is
     * enabled:
     * - In case of instant placement disabled, the z position will be estimated by the AR surface
     * distance at the (x,y) so this value is not used.
     * - In case of instant placement enabled, this value is used as
     * [approximateDistanceMeters][ArFrame.hitTest] to help ARCore positioning result.
     *
     * By default, the node is positioned at the center screen, 2 meters forward
     *
     * **Horizontal (X):**
     * - left: x < 0.0f
     * - center horizontal: x = 0.0f
     * - right: x > 0.0f
     *
     * **Vertical (Y):**
     * - top: y > 0.0f
     * - center vertical : y = 0.0f
     * - bottom: y < 0.0f
     *
     * **Depth (Z):**
     * - forward: z < 0.0f
     * - origin/camera position: z = 0.0f
     * - backward: z > 0.0f
     *
     * ------- +y ----- -z
     *
     * ---------|----/----
     *
     * ---------|--/------
     *
     * -x - - - 0 - - - +x
     *
     * ------/--|---------
     *
     * ----/----|---------
     *
     * +z ---- -y --------
     */
    var cameraPosition: Position = defaultCameraPosition,
    /**
     * TODO : Doc
     *
     * @see io.github.sceneview.ar.node.ArModelNode.placementMode
     */
    placementMode: PlacementMode = defaultPlacementMode,
) : ArNode(position = cameraPosition), ArSceneLifecycleObserver {

    companion object {
        val defaultCameraPosition get() = Position(0.0f, 0.0f, -2.0f)
        val defaultPlacementMode get() = PlacementMode.BEST_AVAILABLE
    }

    /**
     * TODO: Doc
     */
    var placementMode: PlacementMode = defaultPlacementMode
        set(value) {
            field = value
            doOnAttachedToScene { sceneView ->
                (sceneView as? ArSceneView)?.apply {
                    planeFindingMode = when (placementMode) {
                        PlacementMode.DISABLED, PlacementMode.INSTANT -> PlaneFindingMode.DISABLED
                        // TODO: Don't limit whole config instead filter horizontal/vertical hitTests
                        PlacementMode.PLANE_HORIZONTAL -> PlaneFindingMode.HORIZONTAL
                        // TODO: Don't limit whole config instead filter horizontal/vertical hitTests
                        PlacementMode.PLANE_VERTICAL -> PlaneFindingMode.VERTICAL
                        else -> PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    }
                    depthEnabled = placementMode.depthEnabled
                    instantPlacementEnabled = placementMode.instantPlacementEnabled
                }
            }
        }

    var lastTrackedHitResult: HitResult? = null

    /**
     * TODO : Doc
     */
    var onArFrameHitResult: ((node: ArNode, hitResult: HitResult?, isTracking: Boolean) -> Unit)? =
        null


    /**
     * ### Loads a monolithic binary glTF and add it to the Node
     *
     * @param modelLocation the glb file location:
     * - A relative asset file location *models/mymodel.glb*
     * - An android resource from the res folder *context.getResourceUri(R.raw.mymodel)*
     * - A File path *Uri.fromFile(myModelFile).path*
     * - An http or https url *https://mydomain.com/mymodel.glb*
     * @param coroutineScope your Activity or Fragment coroutine scope if you want to preload the
     * 3D model before the node is attached to the [SceneView]
     * @param animate Plays the animations automatically if the model has one
     *
     * @see loadModel
     */
    constructor(
        context: Context,
        modelLocation: String,
        coroutineScope: LifecycleCoroutineScope? = null,
        animate: Boolean = true,
        onModelLoaded: ((instance: RenderableInstance) -> Unit)? = null,
        onError: ((error: Exception) -> Unit)? = null
    ) : this() {
        loadModel(context, modelLocation, coroutineScope, animate, onModelLoaded, onError)
    }

    fun hitTest(): HitResult? = super.hitTest(
        frame = session?.currentFrame,
        xPx = (session?.displayWidth ?: 0) / 2.0f * (1.0f + cameraPosition.x),
        yPx = (session?.displayHeight ?: 0) / 2.0f * (1.0f - cameraPosition.y),
        approximateDistanceMeters = kotlin.math.abs(cameraPosition.z),
        plane = placementMode.planeEnabled,
        depth = placementMode.depthEnabled,
        instantPlacement = placementMode.instantPlacementEnabled
    )

    override fun onArFrame(frame: ArFrame) {
        super<ArNode>.onArFrame(frame)

        if (anchor == null) {
            onArFrameHitResult(hitTest(frame))
        }
    }

    fun onArFrameHitResult(hitResult: HitResult?) {
        // Keep the last position when no more tracking result
        if (hitResult?.isTracking == true) {
            lastTrackedHitResult = hitResult
            hitResult.hitPose?.let { hitPose ->
                pose = hitPose
            }
        }
        onArFrameHitResult?.invoke(this, hitResult, isTracking)
    }

    override fun createAnchor(): Anchor? {
        return super.createAnchor() ?: if (lastTrackedHitResult?.isTracking == true) {
            lastTrackedHitResult?.createAnchor()
        } else null
    }

    override fun clone() = copy(ModelNode())

    fun copy(toNode: ArModelNode = ArModelNode()): ArModelNode = toNode.apply {
        super.copy(toNode)

        cameraPosition = this@ArModelNode.cameraPosition
        placementMode = this@ArModelNode.placementMode
    }
}

enum class PlacementMode {
    /**
     * ### Disable every AR placement
     * @see PlaneFindingMode.DISABLED
     */
    DISABLED,

    /**
     * ### Place and orientate nodes only on horizontal planes
     * @see PlaneFindingMode.HORIZONTAL
     */
    PLANE_HORIZONTAL,

    /**
     * ### Place and orientate nodes only on vertical planes
     * @see PlaneFindingMode.VERTICAL
     */
    PLANE_VERTICAL,

    /**
     * ### Place and orientate nodes on both horizontal and vertical planes
     * @see PlaneFindingMode.HORIZONTAL_AND_VERTICAL
     */
    PLANE_HORIZONTAL_AND_VERTICAL,

    /**
     * ### Place and orientate nodes on every detected depth surfaces
     *
     * Not all devices support this mode. In case on non depth enabled device the placement mode
     * will automatically fallback to [PLANE_HORIZONTAL_AND_VERTICAL].
     * @see Config.DepthMode.AUTOMATIC
     */
    DEPTH,

    /**
     * ### Instantly place only nodes at a fixed orientation and an approximate distance
     *
     * No AR orientation will be provided = fixed +Y pointing upward, against gravity)
     *
     * This mode is currently intended to be used with hit tests against horizontal surfaces.
     * Hit tests may also be performed against surfaces with any orientation, however:
     * - The resulting Instant Placement point will always have a pose with +Y pointing upward,
     * against gravity.
     * - No guarantees are made with respect to orientation of +X and +Z. Specifically, a hit
     * test against a vertical surface, such as a wall, will not result in a pose that's in any
     * way aligned to the plane of the wall, other than +Y being up, against gravity.
     * - The [InstantPlacementPoint]'s tracking method may never become
     * [InstantPlacementPoint.TrackingMethod.FULL_TRACKING] } or may take a long time to reach
     * this state. The tracking method remains
     * [InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE] until a
     * (tiny) horizontal plane is fitted at the point of the hit test.
     */
    INSTANT,

    /**
     * ### Place nodes on every detected surfaces
     *
     * The node will be placed instantly and then adjusted to fit the best accurate, precise,
     * available placement.
     */
    BEST_AVAILABLE;

    val planeEnabled: Boolean
        get() = when (this) {
            PLANE_HORIZONTAL, PLANE_VERTICAL, PLANE_VERTICAL, DEPTH, BEST_AVAILABLE -> true
            else -> false
        }

    val depthEnabled: Boolean
        get() = when (this) {
            DEPTH, BEST_AVAILABLE -> true
            else -> false
        }

    val instantPlacementEnabled: Boolean
        get() = when (this) {
            INSTANT, BEST_AVAILABLE -> true
            else -> false
        }
}
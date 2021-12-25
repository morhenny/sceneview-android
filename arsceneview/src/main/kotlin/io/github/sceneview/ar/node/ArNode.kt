package io.github.sceneview.ar.node

import com.google.android.filament.utils.*
import com.google.ar.core.*
import com.google.ar.core.Config.PlaneFindingMode
import com.google.ar.sceneform.math.Vector3
import io.github.sceneview.*
import io.github.sceneview.ar.ArSceneLifecycle
import io.github.sceneview.ar.ArSceneLifecycleObserver
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.*
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

open class ArNode(
    /**
     * ### The node position
     *
     * The node's position locates it within the coordinate system of its parent.
     * The default position is the zero vector, indicating that the node is placed at the origin of
     * the parent node's coordinate system.
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
     */
    position: Position = defaultPosition,
    /**
     * ### The node orientation in Euler Angles Degrees per axis.
     *
     * `[0..360]`
     *
     * The three-component rotation vector specifies the direction of the rotation axis in degrees.
     *
     * The default rotation is the zero vector, specifying no rotation.
     * Rotation is applied relative to the node's origin property.
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
    rotation: Rotation = defaultRotation,
    /**
     * ### The node scales
     *
     * - reduce size: scale < 1.0f
     * - same size: scale = 1.0f
     * - increase size: scale > 1.0f
     */
    scale: Scale = defaultScale
) : ModelNode(position = position, rotation = rotation, scale = scale), ArSceneLifecycleObserver {

    override val sceneView: ArSceneView? get() = super.sceneView as? ArSceneView
    override val lifecycle: ArSceneLifecycle? get() = sceneView?.lifecycle
    protected val session: ArSession? get() = sceneView?.session

    /**
     * TODO : Doc
     */
    open val isTracking get() = pose != null

    /**
     * TODO : Doc
     */
    var pose: Pose? = null
        set(value) {
            val wasTracking = isTracking
            field = value
            if (value != null) {
                if (smoothChange) {
                    smooth(position = value.position, rotation = value.rotation)
                } else {
                    transform(position = value.position, rotation = value.rotation)
                }
            }
            onPoseChanged(value)
            if(wasTracking != isTracking) {
                onTrackingChanged(isTracking)
            }
        }


    /**
     * TODO : Doc
     */
    val isAnchored get() = anchor != null

    /**
     * TODO : Doc
     */
    var anchor: Anchor? = null
        set(value) {
            field?.detach()
            field = value
            pose = value?.pose
            onAnchorChanged(value)
        }

    /**
     * ### Move smoothly/slowly when there is a pose (AR position and rotation) update
     *
     * Use [smoothSpeed] to change the position/rotation smooth update speed
     */
    var smoothChange = false

    /**
     * TODO : Doc
     */
    var onPoseChanged: ((node: ArNode, pose: Pose?) -> Unit)? = null

    /**
     * TODO : Doc
     */
    var onTrackingChanged: ((node: ArNode, isTracking: Boolean) -> Unit)? = null

    /**
     * TODO : Doc
     */
    val onAnchorChanged = mutableListOf<(node: Node, anchor: Anchor?) -> Unit>()

    /**
     * TODO : Doc
     */
    constructor(anchor: Anchor) : this() {
        this.anchor = anchor
    }

    /**
     * TODO : Doc
     */
    constructor(hitResult: HitResult) : this(hitResult.createAnchor())

    override fun onArFrame(frame: ArFrame) {
        // Update the anchor position if any
        if (anchor?.trackingState == TrackingState.TRACKING) {
            pose = anchor?.pose
        }
    }

    /**
     * TODO : Doc
     */
    open fun onPoseChanged(pose: Pose?) {
        onPoseChanged?.invoke(this, pose)
    }

    /**
     * TODO : Doc
     */
    open fun onTrackingChanged(isTracking: Boolean) {
        onTrackingChanged?.invoke(this, isTracking)
    }


    /**
     * TODO : Doc
     */
    open fun onAnchorChanged(anchor: Anchor?) {
        onAnchorChanged.forEach { it(this, anchor) }
    }

    /**
     * ### Performs a ray cast to retrieve the ARCore info at this camera point
     *
     * @param frame the [ArFrame] from where we take the [HitResult]
     * By default the latest session frame if any exist
     * @param xPx x view coordinate in pixels
     * By default the [cameraPosition.x][cameraPosition] of this Node is used
     * @property yPx y view coordinate in pixels
     * By default the [cameraPosition.y][cameraPosition] of this Node is used
     *
     * @return the hitResult or null if no info is retrieved
     *
     * @see ArFrame.hitTest
     */
    @JvmOverloads
    open fun hitTest(
        frame: ArFrame? = session?.currentFrame,
        xPx: Float = (session?.displayWidth ?: 0) / 2.0f * (1.0f + position.x),
        yPx: Float = (session?.displayHeight ?: 0) / 2.0f * (1.0f - position.y),
        approximateDistanceMeters: Float = kotlin.math.abs(position.z),
        plane: Boolean = (sceneView?.planeFindingMode?: ArSceneView.defaultDepthEnabled) != PlaneFindingMode.DISABLED,
        depth: Boolean = sceneView?.depthEnabled?: ArSceneView.defaultDepthEnabled,
        instantPlacement: Boolean = sceneView?.instantPlacementEnabled?: ArSceneView.defaultInstantPlacementEnabled
    ): HitResult? = frame?.hitTest(xPx, yPx, approximateDistanceMeters, plane, depth, instantPlacement)

    /**
     * ### Creates a new anchor at actual node worldPosition and worldRotation (hit location)
     *
     * Creates an anchor at the given pose in the world coordinate space that is attached to this
     * trackable. The type of trackable will determine the semantics of attachment and how the
     * anchor's pose will be updated to maintain this relationship. Note that the relative offset
     * between the pose of multiple anchors attached to a trackable may adjust slightly over time as
     * ARCore updates its model of the world.
     *
     * Anchors incur ongoing processing overhead within ARCore. To release unneeded anchors use
     * [Anchor.detach]
     *
     * This method is a convenience alias for [HitResult.createAnchor]
     */
    open fun createAnchor(): Anchor? = hitTest()?.createAnchor()

    /**
     * ### Anchor this node to make it fixed at the actual position and orientation is the world
     *
     * Creates an anchor at the given pose in the world coordinate space that is attached to this
     * trackable. The type of trackable will determine the semantics of attachment and how the
     * anchor's pose will be updated to maintain this relationship. Note that the relative offset
     * between the pose of multiple anchors attached to a trackable may adjust slightly over time as
     * ARCore updates its model of the world.
     */
    fun anchor(): Boolean {
        anchor?.detach()
        anchor = createAnchor()
        return anchor != null
    }

    /**
     * ### Creates a new anchored Node at the actual worldPosition and worldRotation
     *
     * The returned node position and rotation will be fixed within camera movements.
     *
     * See [hitTest] and [ArFrame.hitTests] for details.
     *
     * Anchors incur ongoing processing overhead within ARCore.
     * To release unneeded anchors use [destroy].
     */
    open fun createAnchoredNode(): ArNode? {
        return createAnchor()?.let { anchor ->
            ArNode(anchor)
        }
    }

    /**
     * TODO: Doc
     */
    open fun createAnchoredCopy(): ArNode? {
        return createAnchoredNode()?.apply {
            copy(this)
        }
    }

//    private val smouthLerpFactor: Float
//        get() = MathHelper.clamp(
//            (1.0f / (sceneView?.maxFramesPerSeconds
//                ?: defaultMaxFPS) * smoothMoveSpeedFactor),
//            0f,
//            1f
//        )

    override val worldTransform: Mat4
        get() = if (!isAnchored) {
            sceneView?.camera?.let { camera ->
                super.worldTransform * camera.worldTransform
            } ?: super.worldTransform
        } else {
            super.worldTransform
        }

    override fun destroy() {
        super.destroy()

        anchor?.detach()
        anchor = null
    }

    /**
     * ### Converts a point in the local-space of this node to world-space.
     *
     * @param point the point in local-space to convert
     * @return a new vector that represents the point in world-space
     */
    fun localToWorldPosition(point: Vector3) = transformationMatrix.transformPoint(point)

    /**
     * ### Converts a point in world-space to the local-space of this node.
     *
     * @param point the point in world-space to convert
     * @return a new vector that represents the point in local-space
     */
    fun worldToLocalPosition(point: Vector3) = transformationMatrixInverted.transformPoint(point)

    /**
     * ### Converts a direction from the local-space of this node to world-space.
     *
     * Not impacted by the position or scale of the node.
     *
     * @param direction the direction in local-space to convert
     * @return a new vector that represents the direction in world-space
     */
//    fun localToWorldDirection(direction: Vector3) =
//        Quaternion.rotateVector(worldRotationQuaternion, direction)

    /**
     * ### Converts a direction from world-space to the local-space of this node.
     *
     * Not impacted by the position or scale of the node.
     *
     * @param direction the direction in world-space to convert
     * @return a new vector that represents the direction in local-space
     */
//    fun worldToLocalDirection(direction: Vector3) =
//        Quaternion.inverseRotateVector(worldRotationQuaternion, direction)

    /** ### Gets the world-space forward direction vector (-z) of this node */
//    val worldForward get() = localToWorldDirection(Vector3.forward())

    /** ### Gets the world-space back direction vector (+z) of this node */
//    val worldBack get() = localToWorldDirection(Vector3.back())

    /** ### Gets the world-space right direction vector (+x) of this node */
//    val worldRight get() = localToWorldDirection(Vector3.right())

    /** ### Gets the world-space left direction vector (-x) of this node */
//    val worldLeft get() = localToWorldDirection(Vector3.left())

    /** ### Gets the world-space up direction vector (+y) of this node */
//    val worldUp get() = localToWorldDirection(Vector3.up())

    /** ### Gets the world-space down direction vector (-y) of this node */
//    val worldDown get() = localToWorldDirection(Vector3.down())
}
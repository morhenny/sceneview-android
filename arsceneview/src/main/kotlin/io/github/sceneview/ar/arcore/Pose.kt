package io.github.sceneview.ar.arcore

import com.google.android.filament.utils.Float4
import com.google.ar.core.Pose
import io.github.sceneview.Position
import io.github.sceneview.Rotation
import kotlin.math.asin
import kotlin.math.atan2

val Pose.position: Position get() = Position(x = tx(), y = ty(), z = tz())
val Pose.rotation: Rotation
    get() = rotationQuaternion!!.let {
        Float4(
            x = it[0],
            y = it[1],
            z = it[2],
            w = it[3]
        ).eulerAngles
    }

val Float4.eulerAngles: Rotation
    get() {
        val xRadians =
            atan2((2.0f * (y * z + w * x)).toDouble(), (w * w - x * x - y * y + z * z).toDouble())
        val yRadians = asin((-2.0f * (x * z - w * y)).toDouble())
        val zRadians =
            atan2((2.0f * (x * y + w * z)).toDouble(), (w * w + x * x - y * y - z * z).toDouble())
        return Rotation(
            Math.toDegrees(xRadians).toFloat(),
            Math.toDegrees(yRadians).toFloat(),
            Math.toDegrees(zRadians).toFloat()
        )
    }

/**
 * Calculate the normal distance from this to the other, the given other pose should have y axis
 * parallel to plane's normal, for example plane's center pose or hit test pose.
 */
fun Pose.distanceTo(other: Pose): Float {
    val normal = FloatArray(3)
    // Get transformed Y axis of plane's coordinate system.
    getTransformedAxis(1, 1.0f, normal, 0)
    // Compute dot product of plane's normal with vector from camera to plane center.
    return (other.tx() - tx()) * normal[0] + (other.ty() - ty()) * normal[1] + (other.tz() - tz()) * normal[2]
}

// Calculate the normal distance to plane from cameraPose, the given planePose should have y axis
// parallel to plane's normal, for example plane's center pose or hit test pose.
fun Pose.calculateDistanceToPlane(cameraPose: Pose): Float {
    val normal = FloatArray(3)
    val cameraX = cameraPose.tx()
    val cameraY = cameraPose.ty()
    val cameraZ = cameraPose.tz()
    // Get transformed Y axis of plane's coordinate system.
    this.getTransformedAxis(1, 1.0f, normal, 0)
    // Compute dot product of plane's normal with vector from camera to plane center.
    return (cameraX - this.tx()) * normal[0] + (cameraY - this.ty()) * normal[1] + (cameraZ - this.tz()) * normal[2]
}

// Calculate the normal distance to plane from cameraPose, the given planePose should have y axis
// parallel to plane's normal, for example plane's center pose or hit test pose.
//fun Pose.rotation(): Quaternion {
//    val vector3: Vector3
//    val normal = getTransformedAxis(1, 1.0f)
//
//    return Quaternion.lookRotation(Vector3(normal[0], normal[1], normal[2]),  Vector3.up())
//}

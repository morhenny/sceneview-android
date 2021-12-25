package io.github.sceneview

import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderLoader
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Float4
import com.google.ar.sceneform.math.MathHelper
import com.google.ar.sceneform.rendering.EngineInstance
import io.github.sceneview.environment.IBLPrefilter
import io.github.sceneview.material.MaterialLoader

// TODO : Add the lifecycle aware management when filament dependents are all kotlined
object Filament {

    init {
        Filament.init()
    }

    @JvmStatic
    val engine = EngineInstance.getEngine().filamentEngine

    @JvmStatic
    val entityManager
        get() = EntityManager.get()

    val uberShaderLoader by lazy { UbershaderLoader(engine) }

    @JvmStatic
    val assetLoader by lazy {
        AssetLoader(engine, uberShaderLoader, entityManager)
    }

    val transformManager get() = engine.transformManager

    val resourceLoader by lazy { ResourceLoader(engine, true, false) }

    val lightManager get() = engine.lightManager

    val iblPrefilter by lazy { IBLPrefilter(engine) }

    fun destroy() {
        //TODO : Add every Filament destroys
        MaterialLoader.destroy()
    }
}

fun Float4.toFloatArray() = this.let { (x, y, z, w) -> floatArrayOf(x, y, z, w) }
fun FloatArray.toFloat4() = this.let { (x, y, z, w) -> Float4(x, y, z, w) }

fun Float3.toFloatArray() = this.let { (x, y, z) -> floatArrayOf(x, y, z) }
fun FloatArray.toFloat3() = this.let { (x, y, z) -> Float3(x, y, z) }

typealias Position = Float3

fun FloatArray.toPosition() = this.let { (x, y, z) -> Position(x, y, z) }
fun positionOf(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f) = Position(x, y, z)

typealias Rotation = Float3

fun FloatArray.toRotation() = this.let { (x, y, z) -> Rotation(x, y, z) }
fun rotationOf(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f) = Rotation(x, y, z)

typealias Scale = Float3

fun FloatArray.toScale() = this.let { (x, y, z) -> Scale(x, y, z) }
fun scaleOf(x: Float = 1.0f, y: Float = 1.0f, z: Float = 1.0f) = Scale(x, y, z)

typealias Direction = Float3

fun FloatArray.toDirection() = this.let { (x, y, z) -> Direction(x, y, z) }
fun directionOf(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f) = Direction(x, y, z)

typealias Color = Float4

fun FloatArray.toColor() = Color(this[0], this[1], this[2], this.getOrNull(3) ?: 1.0f)
fun colorOf(r: Float = 0.0f, g: Float = 0.0f, b: Float = 0.0f, a: Float = 1.0f) = Color(r, g, b, a)
fun colorOf(array: List<Float> = listOf(0.0f, 0.0f, 0.0f)) = Color(array[0], array[1], array[2])

/**
 * ### Linearly interpolates between start and end.
 *
 * @param start the beginning value
 * @param end the ending value
 * @param ratio ratio between the two floats.
 * @return interpolated value between the two floats
 */
//TODO : Move to vector calculation directly
fun lerp(start: Float3, end: Float3, ratio: Float) = Float3(
    x = MathHelper.lerp(start.x, end.x, ratio),
    y = MathHelper.lerp(start.y, end.y, ratio),
    z = MathHelper.lerp(start.z, end.z, ratio)
)


/**
 * ### Linearly interpolates between start and end by a ratio.
 *
 * @param start the beginning value
 * @param end the ending value
 * @param ratio ratio between the two floats
 * @return interpolated value between the two floats
 */
fun lerp(start: Float, end: Float, ratio: Float): Float {
    return start + ratio * (end - start)
}

package io.github.sceneview.model

import android.content.Context
import android.net.Uri
import androidx.annotation.IntRange
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.CollisionShape
import com.google.ar.sceneform.common.TransformProvider
import com.google.ar.sceneform.math.Matrix
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.resources.ResourceRegistry
import com.google.ar.sceneform.utilities.AndroidPreconditions
import com.google.ar.sceneform.utilities.LoadHelper
import com.google.ar.sceneform.utilities.Preconditions
import java.io.InputStream
import java.lang.AssertionError
import java.lang.IllegalArgumentException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Function


const val modelDefaultRenderPriority = 4
const val firstRenderPriority = 0
const val lastRenderPriority = 7

/**
 * The default number of frames per seconds for this model animation
 */
const val defaultAnimationFrameRate = 24

/**
 * ### 3D model object
 *
 * Base class for rendering in 3D space by attaching to a [io.github.sceneview.node.ModelNode] with
 * [io.github.sceneview.node.ModelNode.setModel].
 */
//abstract class Model {
//
//    lateinit var asset: FilamentAsset
//    lateinit var instance: FilamentInstance
//
//    protected var progressiveLoad: Boolean
//
//    // Data that is unique per-Renderable.
//    val materialBindings = ArrayList<Material>()
//    val materialNames = ArrayList<String>()
//    private var renderPriority = modelDefaultRenderPriority
//    private var isShadowCaster = true
//    private var isShadowReceiver = true
//
//    /**
//     * Gets the number of frames per seconds defined in the asset animation.
//     */
//    //The number of frames per seconds defined in the asset
//    var animationFrameRate: Int
//        private set
//
//    private var hasChanged = false
//
//    /**
//     * ### The [CollisionShape] used for collision detection with this [Model]
//     */
//    var collisionShape: CollisionShape? = null
//        set(value) {
//            field = value
//            onChanged()
//        }
//
//
//    /**
//     * @hide
//     */
//    // Suppress @UnderInitialization warning.
//    protected constructor(builder: Builder<out Renderable?, out Builder<*, *>?>) {
//        Preconditions.checkNotNull(builder, "Parameter \"builder\" was null.")
//        renderableData = if (builder.isFilamentAsset) {
//            RenderableInternalFilamentAssetData()
//        } else if (builder.isGltf) {
//            createRenderableInternalGltfData()
//        } else {
//            RenderableInternalData()
//        }
//        if (builder.definition != null) {
//            updateFromDefinition(builder.definition)
//        }
//        progressiveLoad = builder.asyncLoadEnabled
//        animationFrameRate = builder.animationFrameRate
//    }
//
//    protected constructor(other: Renderable) {
//        if (other.id.isEmpty) {
//            throw AssertionError("Cannot copy uninitialized Renderable.")
//        }
//
//        // Share renderableData with the original Renderable.
//        renderableData = other.renderableData
//
//        // Copy materials.
//        Preconditions.checkState(other.materialNames.size == other.materialBindings.size)
//        for (i in other.materialBindings.indices) {
//            val otherMaterial = other.materialBindings[i]
//            materialBindings.add(otherMaterial.makeCopy())
//            materialNames.add(other.materialNames[i])
//        }
//        renderPriority = other.renderPriority
//        isShadowCaster = other.isShadowCaster
//        isShadowReceiver = other.isShadowReceiver
//
//        // Copy collision shape.
//        if (other.collisionShape != null) {
//            collisionShape = other.collisionShape!!.makeCopy()
//        }
//        progressiveLoad = other.asyncLoadEnabled
//        animationFrameRate = other.animationFrameRate
//        id.update()
//    }
//
//    private fun onChanged() {
//    }
//
//    /**
//     * ### The material bound to the first/default submesh.
//     */
//    var material: Material
//        get() = getMaterial(0)
//        set(material) {
//            setMaterial(0, material)
//        }
//
//    /**
//     * Returns the material bound to the specified submesh.
//     */
//    fun getMaterial(submeshIndex: Int): Material {
//        if (submeshIndex < materialBindings.size) {
//            return materialBindings[submeshIndex]
//        }
//        throw makeSubmeshOutOfRangeException(submeshIndex)
//    }
//
//    /**
//     * Sets the material bound to the specified submesh.
//     */
//    fun setMaterial(submeshIndex: Int, material: Material) {
//        if (submeshIndex < materialBindings.size) {
//            materialBindings[submeshIndex] = material
//            onChanged()
//        } else {
//            throw makeSubmeshOutOfRangeException(submeshIndex)
//        }
//    }
//
//    /**
//     * Returns the name associated with the specified submesh.
//     *
//     * @throws IllegalArgumentException if the index is out of range
//     */
//    fun getSubmeshName(submeshIndex: Int): String {
//        Preconditions.checkState(materialNames.size == materialBindings.size)
//        if (submeshIndex >= 0 && submeshIndex < materialNames.size) {
//            return materialNames[submeshIndex]
//        }
//        throw makeSubmeshOutOfRangeException(submeshIndex)
//    }
//
//    /**
//     * Get the render priority that controls the order of rendering. The priority is between a range
//     * of 0 (rendered first) and 7 (rendered last). The default value is 4.
//     */
//    fun getRenderPriority(): Int {
//        return renderPriority
//    }
//
//    /**
//     * Set the render priority to control the order of rendering. The priority is between a range of 0
//     * (rendered first) and 7 (rendered last). The default value is 4.
//     */
//    fun setRenderPriority(
//        @IntRange(
//            from = firstRenderPriority.toLong(),
//            to = lastRenderPriority.toLong()
//        ) renderPriority: Int
//    ) {
//        this.renderPriority =
//            Math.min(lastRenderPriority, Math.max(firstRenderPriority, renderPriority))
//        id.update()
//    }
//
//    /**
//     * Returns true if configured to cast shadows on other renderables.
//     */
//    fun isShadowCaster(): Boolean {
//        return isShadowCaster
//    }
//
//    /**
//     * Sets whether the renderable casts shadow on other renderables in the scene.
//     */
//    fun setShadowCaster(isShadowCaster: Boolean) {
//        this.isShadowCaster = isShadowCaster
//        id.update()
//    }
//
//    /**
//     * Returns true if configured to receive shadows cast by other renderables.
//     */
//    fun isShadowReceiver(): Boolean {
//        return isShadowReceiver
//    }
//
//    /**
//     * Sets whether the renderable receives shadows cast by other renderables in the scene.
//     */
//    fun setShadowReceiver(isShadowReceiver: Boolean) {
//        this.isShadowReceiver = isShadowReceiver
//        id.update()
//    }
//
//    /**
//     * Returns the number of submeshes that this renderable has. All Renderables have at least one.
//     */
//    val submeshCount: Int
//        get() = renderableData!!.getMeshes().size
//
//    /**
//     * @hide
//     */
//    fun createInstance(transformProvider: TransformProvider?): RenderableInstance {
//        return RenderableInstance(transformProvider, this)
//    }
//
//    fun updateFromDefinition(definition: RenderableDefinition?) {
//        Preconditions.checkState(!definition!!.getSubmeshes().isEmpty())
//        id.update()
//        definition.applyDefinitionToData(renderableData, materialBindings, materialNames)
//        collisionShape = Box(renderableData!!.getSizeAabb(), renderableData.getCenterAabb())
//    }
//
//    /**
//     * Creates a new instance of this Renderable.
//     *
//     *
//     * The new renderable will have unique copy of all mutable state. All materials referenced by
//     * the Renderable will also be instanced. Immutable data will be shared between the instances.
//     */
//    abstract fun makeCopy(): Renderable?
//
//    /**
//     * Optionally override in subclasses for work that must be done each frame for specific types of
//     * Renderables. For example, ViewRenderable uses this to prevent the renderable from being visible
//     * until the view has been successfully drawn to an external texture, and initializing material
//     * parameters.
//     */
//    fun prepareForDraw() {
//        if (renderableData is RenderableInternalFilamentAssetData) {
//            val renderableData = renderableData as RenderableInternalFilamentAssetData?
//            // Allow the resource loader to finalize textures that have become ready.
//            renderableData!!.resourceLoader.asyncUpdateLoad()
//        }
//    }
//
//    fun attachToRenderer(renderer: Renderer?) {}
//    fun detatchFromRenderer() {}
//
//    /**
//     * Gets the final model matrix to use for rendering this [Renderable] based on the matrix
//     * passed in. Default implementation simply passes through the original matrix. WARNING: Do not
//     * modify the originalMatrix! If the final matrix isn't the same as the original matrix, then a
//     * new instance must be returned.
//     *
//     * @hide
//     */
//    fun getFinalModelMatrix(originalMatrix: Matrix?): Matrix? {
//        Preconditions.checkNotNull(originalMatrix, "Parameter \"originalMatrix\" was null.")
//        return originalMatrix
//    }
//
//    private fun makeSubmeshOutOfRangeException(submeshIndex: Int): IllegalArgumentException {
//        return IllegalArgumentException(
//            "submeshIndex ("
//                    + submeshIndex
//                    + ") is out of range. It must be less than the submeshCount ("
//                    + submeshCount
//                    + ")."
//        )
//    }
//
//    private fun createRenderableInternalGltfData(): IRenderableInternalData? {
//        return null
//    }
//
//    /**
//     * Used to programmatically construct a [Renderable]. Builder data is stored, not copied. Be
//     * careful when modifying the data before or between build calls.
//     */
//    // CompletableFuture
//    abstract class Builder<T : Renderable?, B : Builder<T, B>?>
//    /**
//     * Used to programmatically construct a [Renderable].
//     */
//    protected constructor() {
//        /**
//         * @hide
//         */
//        protected var registryId: Any? = null
//
//        /**
//         * @hide
//         */
//        protected var context: Context? = null
//        private var sourceUri: Uri? = null
//        private var inputStreamCreator: Callable<InputStream>? = null
//        var definition: RenderableDefinition? = null
//        val isGltf = false
//        var isFilamentAsset = false
//        var asyncLoadEnabled = false
//        private val loadGltfListener: LoadGltfListener? = null
//        private val uriResolver: Function<String, Uri>? = null
//        private val materialsBytes: ByteArray? = null
//        var animationFrameRate = defaultAnimationFrameRate
//        fun setSource(context: Context?, inputStreamCreator: Callable<InputStream>): B {
//            Preconditions.checkNotNull(inputStreamCreator)
//            sourceUri = null
//            this.inputStreamCreator = inputStreamCreator
//            this.context = context
//            return self
//        }
//
//        fun setSource(context: Context, sourceUri: Uri): B {
//            return setRemoteSourceHelper(context, sourceUri, true)
//        }
//
//        fun setSource(context: Context?, sourceUri: Uri?, enableCaching: Boolean): B? {
//            return null
//        }
//
//        fun setSource(context: Context?, resource: Int): B {
//            inputStreamCreator = LoadHelper.fromResource(context, resource)
//            this.context = context
//            val uri = LoadHelper.resourceToUri(context, resource)
//            sourceUri = uri
//            registryId = uri
//            return self
//        }
//
//        /**
//         * Build a [Renderable] from a [RenderableDefinition].
//         */
//        fun setSource(definition: RenderableDefinition?): B {
//            this.definition = definition
//            registryId = null
//            sourceUri = null
//            return self
//        }
//
//        fun setRegistryId(registryId: Any?): B {
//            this.registryId = registryId
//            return self
//        }
//
//        fun setIsFilamentGltf(isFilamentGltf: Boolean): B {
//            isFilamentAsset = isFilamentGltf
//            return self
//        }
//
//        /**
//         * Enable textures async loading after first rendering.
//         * Default is false.
//         */
//        fun setAsyncLoadEnabled(asyncLoadEnabled: Boolean): B {
//            this.asyncLoadEnabled = asyncLoadEnabled
//            return self
//        }
//
//        /**
//         * Sets the number of frames per seconds defined in the asset.
//         *
//         * @param frameRate The number of frames during one second
//         */
//        fun setAnimationFrameRate(frameRate: Int): B {
//            animationFrameRate = frameRate
//            return self
//        }
//
//        /**
//         * True if a source function will be called during build
//         *
//         * @hide
//         */
//        fun hasSource(): Boolean {
//            return sourceUri != null || inputStreamCreator != null || definition != null
//        }
//
//        /**
//         * Constructs a [Renderable] with the parameters of the builder.
//         *
//         * @return the constructed [Renderable]
//         */
//        fun build(): CompletableFuture<T> {
//            try {
//                checkPreconditions()
//            } catch (failedPrecondition: Throwable) {
//                val result = CompletableFuture<T>()
//                result.completeExceptionally(failedPrecondition)
//                FutureHelper.logOnException(
//                    renderableClass.simpleName,
//                    result,
//                    "Unable to load Renderable registryId='$registryId'"
//                )
//                return result
//            }
//
//            // For static-analysis check.
//            val registryId = registryId
//            if (registryId != null) {
//                // See if a renderable has already been registered by this id, if so re-use it.
//                val registry = renderableRegistry
//                val renderableFuture = registry[registryId]
//                if (renderableFuture != null) {
//                    return renderableFuture.thenApply(
//                        Function { renderable: T ->
//                            renderableClass.cast(
//                                renderable!!.makeCopy()
//                            )
//                        })
//                }
//            }
//            val renderable = makeRenderable()
//            if (definition != null) {
//                return CompletableFuture.completedFuture(renderable)
//            }
//
//            // For static-analysis check.
//            val inputStreamCreator = inputStreamCreator
//            if (inputStreamCreator == null) {
//                val result = CompletableFuture<T>()
//                result.completeExceptionally(AssertionError("Input Stream Creator is null."))
//                FutureHelper.logOnException(
//                    renderableClass.simpleName,
//                    result,
//                    "Unable to load Renderable registryId='$registryId'"
//                )
//                return result
//            }
//            var result: CompletableFuture<T>? = null
//            result = if (isFilamentAsset) {
//                if (context != null) {
//                    loadRenderableFromFilamentGltf(context!!, renderable)
//                } else {
//                    throw AssertionError("Gltf Renderable.Builder must have a valid context.")
//                }
//            } else if (isGltf) {
//                if (context != null) {
//                    loadRenderableFromGltf(context!!, renderable, materialsBytes)
//                } else {
//                    throw AssertionError("Gltf Renderable.Builder must have a valid context.")
//                }
//            } else {
//                val loader = LoadRenderableFromSfbTask(renderable, sourceUri)
//                loader.downloadAndProcessRenderable(inputStreamCreator)
//            }
//            if (registryId != null) {
//                val registry = renderableRegistry
//                registry.register(registryId, result)
//            }
//            FutureHelper.logOnException(
//                renderableClass.simpleName,
//                result,
//                "Unable to load Renderable registryId='$registryId'"
//            )
//            return result!!.thenApply { resultRenderable: T ->
//                renderableClass.cast(
//                    resultRenderable!!.makeCopy()
//                )
//            }
//        }
//
//        protected fun checkPreconditions() {
//            AndroidPreconditions.checkUiThread()
//            if (!hasSource()) {
//                throw AssertionError("ModelRenderable must have a source.")
//            }
//        }
//
//        private fun setRemoteSourceHelper(
//            context: Context,
//            sourceUri: Uri,
//            enableCaching: Boolean
//        ): B {
//            Preconditions.checkNotNull(sourceUri)
//            this.sourceUri = sourceUri
//            this.context = context
//            registryId = sourceUri
//            // Configure caching.
//            if (enableCaching) {
//                setCachingEnabled(context)
//            }
//            val connectionProperties: MutableMap<String, String> = HashMap()
//            if (!enableCaching) {
//                connectionProperties["Cache-Control"] = "no-cache"
//            } else {
//                connectionProperties["Cache-Control"] =
//                    "max-stale=" + DEFAULT_MAX_STALE_CACHE
//            }
//            inputStreamCreator = LoadHelper.fromUri(
//                context, Preconditions.checkNotNull(this.sourceUri), connectionProperties
//            )
//            return self
//        }
//
//        private fun loadRenderableFromGltf(
//            context: Context, renderable: T, materialsBytes: ByteArray?
//        ): CompletableFuture<T>? {
//            return null
//        }
//
//        private fun loadRenderableFromFilamentGltf(
//            context: Context, renderable: T
//        ): CompletableFuture<T> {
//            val loader = LoadRenderableFromFilamentGltfTask(
//                renderable, context, Preconditions.checkNotNull(sourceUri), uriResolver
//            )
//            return loader.downloadAndProcessRenderable(Preconditions.checkNotNull(inputStreamCreator))
//        }
//
//        private fun setCachingEnabled(context: Context) {
//            return
//        }
//
//        protected abstract fun makeRenderable(): T
//        protected abstract val renderableClass: Class<T>
//        protected abstract val renderableRegistry: ResourceRegistry<T>
//        protected abstract val self: B
//    }
//
//    companion object {
//
//
//        // Allow stale data two weeks old by default.
//        private val DEFAULT_MAX_STALE_CACHE = TimeUnit.DAYS.toSeconds(14)
//
//    }
//}
package io.github.sceneview.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.filament.Material
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.KTXLoader
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import io.github.sceneview.Filament
import io.github.sceneview.environment.HDREnvironment
import io.github.sceneview.environment.createEnvironment
import io.github.sceneview.environment.defaultSpecularFilter
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.material.MaterialLoader
import io.github.sceneview.material.use
import io.github.sceneview.utils.fileBuffer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.nio.Buffer
import java.nio.ByteBuffer

//object GlbLoader2 {
//
//    var cache = mutableMapOf<String, ByteBuffer>()
//
//    /**
//     * ### Utility for loading a glTF 3D model
//     *
//     * @param glbFileLocation the glb file location:
//     * - A relative asset file location *models/mymodel.glb*
//     * - An android resource from the res folder *context.getResourceUri(R.raw.mymodel)*
//     * - A File path *Uri.fromFile(myModelFile).path*
//     * - An http or https url *https://mydomain.com/mymodel.glb*
//     */
//    @JvmOverloads
//    suspend fun loadModel(
//        context: Context,
//        glbFileLocation: String
//    ): ModelRenderable?  {
//        return try {
//            cache[glbFileLocation]?.createInstance()
//            context.fileBuffer(glbFileLocation)?.let { buffer ->
//                withContext(Dispatchers.Main) {
//                    createEnvironment(buffer, specularFilter)
//                }
//            }
//        } finally {
//            // TODO: See why the finally is called before the onDestroy()
////        environment?.destroy()
//        }
//    }
//
//    @JvmOverloads
//    fun createModel(
//        modelBuffer: Buffer
//    ) = Filament.assetLoader.createAssetFromBinary(modelBuffer)?.let { asset ->
//        asset.
//        Filament.iblPrefilter.equirectangularToCubemap(hdrTexture)
//    }?.let { cubemap ->
//        HDREnvironment(cubemap = cubemap, skyboxEnvironment = cubemap, specularFilter = specularFilter)
//    }
//
//    /**
//     * ### Utility for loading a glTF 3D model
//     *
//     * For Java compatibility usage.
//     *
//     * Kotlin developers should use [GlbLoader.loadModel]
//     *
//     * [Documentation][GlbLoader.loadEnvironment]
//     *
//     */
//    fun loadModelAsync(
//        context: Context,
//        glbFileLocation: String,
//        coroutineScope: LifecycleCoroutineScope,
//        result: (ModelRenderable?) -> Unit
//    ) = coroutineScope.launchWhenCreated {
//        HDRLoader.loadEnvironment()
//        result(
//            loadModel(context, glbFileLocation)
//        )
//    }
//}
//
///**
// *
// * ### Load a Renderable in a coroutine scope without blocking a thread
// *
// * This suspending function is cancellable.
// * If the Job of the current coroutine is cancelled or completed while this suspending function
// * is waiting, this function stops waiting for the completion stage and immediately resumes with
// * CancellationException.
// * This method is intended to be used with one-shot futures, so on coroutine cancellation the
// * CompletableFuture that corresponds to this CompletionStage
// * (see CompletionStage.toCompletableFuture) is cancelled.
// * If cancelling the given stage is undesired, stage.asDeferred().await() should be used instead.
// *
// * @return the created directional light
// *
// * @see [KTXLoader.loadEnvironment]
// * @see [HDRLoader.loadEnvironment]
// */
//suspend fun <T : Renderable, B : Renderable.Builder<T, B>> Renderable.Builder<T, B>.build(
//    coroutineScope: LifecycleCoroutineScope
//) {
//    coroutineScope.launchWhenCreated {
//        await()
//    }
//}
//
///**
// *
// * ### Awaits for loading a Renderable with the parameters of the builder without blocking a thread
// *
// * This suspending function is cancellable.
// * If the Job of the current coroutine is cancelled or completed while this suspending function
// * is waiting, this function stops waiting for the completion stage and immediately resumes with
// * CancellationException.
// * This method is intended to be used with one-shot futures, so on coroutine cancellation the
// * CompletableFuture that corresponds to this CompletionStage
// * (see CompletionStage.toCompletableFuture) is cancelled.
// * If cancelling the given stage is undesired, stage.asDeferred().await() should be used instead.
// *
// * @return the created directional light
// *
// * @see [KTXLoader.loadEnvironment]
// * @see [HDRLoader.loadEnvironment]
// */
//suspend fun <T : Renderable, B : Renderable.Builder<T, B>> Renderable.Builder<T, B>.await() =
//    build().await()
//
///**
// * ### Deferred renderable loading is a non-blocking cancellable future.
// *
// * It is a [Job] with a result.
// *
// * @see [Deferred]
// */
//fun <T : Renderable, B : Renderable.Builder<T, B>> Renderable.Builder<T, B>.asDeferred() =
//    build().asDeferred()
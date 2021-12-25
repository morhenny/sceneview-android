package com.gorisse.thomas.sceneview.sample.ardepthnode

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.DepthNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.utils.doOnApplyWindowInsets

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton

    lateinit var depthNode: DepthNode

    private val rotateAnimator by lazy {
        ObjectAnimator.ofFloat(depthNode.position, "y", 0.0f,360.0f).apply {
            interpolator = LinearInterpolator()
            duration = 2500
            repeatCount = ObjectAnimator.INFINITE
        }
    }

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        sceneView = view.findViewById(R.id.sceneView)
        loadingView = view.findViewById(R.id.loadingView)
        actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener(::actionButtonClicked)
        }

        isLoading = true
        depthNode = DepthNode(
            context = requireContext(),
            coroutineScope = lifecycleScope,
            modelGlbFileLocation = "models/halloween.glb",
            onModelLoaded = { modelInstance ->
                isLoading = false
                modelInstance.animate(true).start()
            }).apply {
            // This 3D model is actually body centered so we change it contentPositionY to the
            // bottom (centerY on his feet)
            // We could also had changed the positionY in order to make it AR placed downer on the
            // screen instead of the screen center.
            contentPosition.y = -1.0f
            contentRotation.y = 180.0f
//            positionY = -0.5f
            onTrackingChanged = { _, isTracking ->
                actionButton.isGone = !isTracking
            }
            sceneView.addChild(this)
        }
        rotateAnimator.start()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuInstantPlacement -> {
                item.isChecked = !item.isChecked
                true
            }
            R.id.menuDepthPlacement -> {
                item.isChecked = !item.isChecked
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun actionButtonClicked(view: View? = null) {
        if (!depthNode.isAnchored && depthNode.anchor()) {
//            rotateAnimator.pause()
            actionButton.text = getString(R.string.move_object)
            actionButton.icon = resources.getDrawable(R.drawable.ic_target)
        } else {
            depthNode.anchor = null
//            rotateAnimator.start()
            actionButton.text = getString(R.string.place_object)
            actionButton.icon = resources.getDrawable(R.drawable.ic_anchor)
        }
    }
}
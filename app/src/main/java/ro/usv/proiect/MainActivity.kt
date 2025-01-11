package ro.usv.proiect

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.VideoNode
import kotlin.rem
import kotlin.text.get

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var selectButton: Button
    private lateinit var rotateLeftButton: Button
    private lateinit var rotateRightButton: Button
    private var currentModelIndex = 0
    private var modelFiles = mutableListOf(
        "models/sofa.glb",
        "models/office_chair.glb",
        "models/shelf.glb",
        "models/sofa_leather.glb",
        "models/computer_desk.glb",
    )
    private val placedModels = mutableListOf<ArModelNode>()
    private val rotationAngle = 45f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.ad)

        placeButton = findViewById(R.id.place)

        selectButton = findViewById<Button>(R.id.select)
        rotateLeftButton = findViewById<Button>(R.id.leftButton)
        rotateRightButton = findViewById<Button>(R.id.rightButton)

        selectButton.setOnClickListener {
            switchModel()
        }

        placeButton.setOnClickListener {
            placeModel()
        }

        rotateLeftButton.setOnClickListener {
            rotateModel(-rotationAngle)
        }
        rotateRightButton.setOnClickListener {
            rotateModel(rotationAngle)
        }


        videoNode = VideoNode(
            sceneView.engine,
            scaleToUnits = 0.7f,
            centerOrigin = Position(y = -4f),
            player = mediaPlayer,
            onLoaded = { _, _ ->
                mediaPlayer.start()
            })

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelFiles[currentModelIndex],
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)

            )
            {
                sceneView.planeRenderer.isVisible = true
                val materialInstance = it.materialInstances[0]
            }

        }
        sceneView.addChild(modelNode)
        modelNode.addChild(videoNode)

        setupInitialModelNode()

    }

    private fun placeModel() {

        val newModelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelFiles[currentModelIndex],
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            ) {
                rotation = modelNode.rotation
                anchor()
            }
        }

        sceneView.addChild(newModelNode)
        placedModels.add(newModelNode)

    }

    private fun setupInitialModelNode() {
        modelNode.loadModelGlbAsync(
            glbFileLocation = modelFiles[currentModelIndex],
            scaleToUnits = 1f,
            centerOrigin = Position(-0.5f)
        )

    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun switchModel() {
        currentModelIndex = (currentModelIndex + 1) % modelFiles.size
        modelNode.loadModelGlbAsync(
            glbFileLocation = modelFiles[currentModelIndex],
            scaleToUnits = 1f,
            centerOrigin = Position(-0.5f)
        )
    }

    private fun rotateModel(angle: Float) {
        if (::modelNode.isInitialized) {
            val currentRotation = modelNode.rotation
            modelNode.rotation = Rotation(
                x = currentRotation.x,
                y = currentRotation.y + angle,
                z = currentRotation.z
            )
        }
    }
}
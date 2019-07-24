package com.mercku.layouthouse.model3d

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.MotionEvent
import com.mercku.layouthouse.ImageEditingView.House
import com.mercku.layouthouse.R
import org.rajawali3d.cameras.ArcballCamera
import org.rajawali3d.lights.DirectionalLight
import org.rajawali3d.materials.Material
import org.rajawali3d.materials.textures.ATexture
import org.rajawali3d.materials.textures.Texture
import org.rajawali3d.renderer.RajawaliRenderer
import org.rajawali3d.surface.RajawaliSurfaceView
import kotlin.math.max

class Model3DActivity : AppCompatActivity() {

    companion object {
        const val HOUSE_LIST = "house list"
    }

    private lateinit var surfaceView: RajawaliSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_3d)
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.setEGLContextClientVersion(2)
        val houseList = intent.getParcelableArrayListExtra<House>(HOUSE_LIST)
        if (houseList.isNullOrEmpty()) {
            finish()
            return
        }
        val coordList = getRoomCuboldCoordinateList(houseList)
        surfaceView.setSurfaceRenderer(object : RajawaliRenderer(this) {
            override fun onOffsetsChanged(
                xOffset: Float,
                yOffset: Float,
                xOffsetStep: Float,
                yOffsetStep: Float,
                xPixelOffset: Int,
                yPixelOffset: Int
            ) {

            }

            override fun onTouchEvent(event: MotionEvent?) {
            }

            override fun initScene() {
                val light = DirectionalLight()
                light.setPosition(0.0, 0.0, 4.0)
                light.setLookAt(0.0, 0.0, 0.3)
                light.enableLookAt()
                light.power = 1.5f
                currentScene.addLight(light)

                for (index in coordList.indices) {
                    val room3DCoordinate = coordList[index]

                    val material = Material()
                    if (room3DCoordinate.isFloor) {
                        material.colorInfluence = 0f
//                        material.enableLighting(true)
//                        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
//                        val canvas = Canvas(bitmap)
//                        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
//                        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//                        paint.color = Color.BLACK
//                        paint.textSize = 20f
//                        canvas.drawText(room3DCoordinate.text, 100.0f, 100.0f, paint)
//                        val texture = AlphaMapTexture("$index", bitmap)
                        val texture = Texture("text", R.drawable.img_bg)
                        try {
                            material.addTexture(texture)
                        } catch (e: ATexture.TextureException) {
                            e.printStackTrace()
                        }
                    } else {
                        material.color = Color.DKGRAY
                    }
                    val room = Cuboid(room3DCoordinate, !room3DCoordinate.isFloor)
                    room.material = material
                    currentScene.addChild(room)
                }
                val arcball = ArcballCamera(mContext, surfaceView)
                arcball.setPosition(0.0, 0.0, 4.0)
                arcball.setLookAt(0.0, 0.0, 0.0)
                currentScene.replaceAndSwitchCamera(currentCamera, arcball)
            }
        })
    }

    private fun getRoomCuboldCoordinateList(room2DCoordinateList: ArrayList<House>): List<Cuboid3DCoordinate> {
        var minLeft = room2DCoordinateList[0].rect.left
        var minTop = room2DCoordinateList[0].rect.top
        var maxRight = 0f
        var maxBottom = 0f
        for (room in room2DCoordinateList) {
            if (room.rect.left < minLeft) {
                minLeft = room.rect.left
            }
            if (room.rect.top < minTop) {
                minTop = room.rect.top
            }
            if (room.rect.right > maxRight) {
                maxRight = room.rect.right
            }
            if (room.rect.bottom > maxBottom) {
                maxBottom = room.rect.bottom
            }
        }
        val viewWidth = resources.displayMetrics.widthPixels
        val viewHeight = resources.displayMetrics.heightPixels
        //求缩放比例
        val widthProportion = maxRight.minus(minLeft).div(viewWidth.minus(20f))
        val heightProportion = maxBottom.minus(minTop).div(viewHeight.minus(40f))
        val proportion = max(widthProportion, heightProportion)

        val newWidth = maxRight.minus(minLeft).div(proportion)
        val newHeight = maxBottom.minus(minTop).div(proportion)

        //找到放缩后的中心点
        val centerX = minLeft.div(proportion) + newWidth.div(2)
        val centerY = minTop.div(proportion) + newHeight.div(2)

        //先放缩,再移动
        for (room in room2DCoordinateList) {
            room.rect.left = (room.rect.left).div(proportion).minus(centerX)
            room.rect.top = -room.rect.top.div(proportion).minus(centerY)
            room.rect.right = room.rect.right.div(proportion).minus(centerX)
            room.rect.bottom = -room.rect.bottom.div(proportion).minus(centerY)
        }

        //转换成OpenGL坐标系中的坐标
        val room3DList = mutableListOf<Cuboid3DCoordinate>()
        val wallWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)

        val widthDistance = viewWidth.minus(10f).div(2)
        val heightDistance = viewHeight.minus(20f).div(2)
        val distance = max(widthDistance, heightDistance)

        for (room in room2DCoordinateList) {
            val outerLeft = room.rect.left.minus(wallWidth).div(distance)
            val outerRight = room.rect.right.plus(wallWidth).div(distance)
            val outerTop = room.rect.top.plus(wallWidth).div(distance)
            val outerBottom = room.rect.bottom.minus(wallWidth).div(distance)

            val innerLeft = room.rect.left.plus(wallWidth).div(distance)
            val innerRight = room.rect.right.minus(wallWidth).div(distance)
            val innerTop = room.rect.top.minus(wallWidth).div(distance)
            val innerBottom = room.rect.bottom.plus(wallWidth).div(distance)

            val leftWall = Cuboid3DCoordinate(
                Coordinate2D(outerLeft, outerTop),
                Coordinate2D(innerLeft, outerTop),
                Coordinate2D(outerLeft, outerBottom),
                Coordinate2D(innerLeft, outerBottom),
                false
            )
            val backWall = Cuboid3DCoordinate(
                Coordinate2D(innerLeft, outerTop),
                Coordinate2D(innerRight, outerTop),
                Coordinate2D(innerLeft, innerTop),
                Coordinate2D(innerRight, innerTop),
                false
            )
            val rightWall = Cuboid3DCoordinate(
                Coordinate2D(innerRight, outerTop),
                Coordinate2D(outerRight, outerTop),
                Coordinate2D(innerRight, outerBottom),
                Coordinate2D(outerRight, outerBottom),
                false
            )
            val frontWall = Cuboid3DCoordinate(
                Coordinate2D(innerLeft, innerBottom),
                Coordinate2D(innerRight, innerBottom),
                Coordinate2D(innerLeft, outerBottom),
                Coordinate2D(innerRight, outerBottom),
                false
            )
            val bottomWall = Cuboid3DCoordinate(
                Coordinate2D(outerLeft, outerTop),
                Coordinate2D(outerRight, outerTop),
                Coordinate2D(outerLeft, outerBottom),
                Coordinate2D(outerRight, outerBottom),
                true, "${room.name}\n${room.area}"
            )
            room3DList.add(leftWall)
            room3DList.add(backWall)
            room3DList.add(rightWall)
            room3DList.add(frontWall)
            room3DList.add(bottomWall)
        }
        return room3DList
    }

}
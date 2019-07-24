/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.mercku.layouthouse.model3d

import android.opengl.GLES20
import org.rajawali3d.Object3D

/**
 * A cube primitive. The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 *
 *
 * When creating solid color cube both `createTextureCoordinates` and `createVertexColorBuffer`
 * can be set to `false`.
 *
 *
 * When creating a textured cube `createTextureCoordinates` should be set to `true` and
 * `createVertexColorBuffer` should be set to `false`.
 *
 *
 * When creating a cube without a texture but with different colors per texture `createTextureCoordinates`
 * should be set to `false` and `createVertexColorBuffer` should be set to `true`.
 *
 * @author dennis.ippel
 */
class Cuboid constructor(coord: Cuboid3DCoordinate, mCreateTextureCoords: Boolean = true) : Object3D() {

    init {
        val wallHeight = if (coord.isFloor) {
            0.0f
        } else {
            0.3f
        }
        val vertices: FloatArray? = floatArrayOf(
            //0-1-2-3 Top
            coord.rightTop.x, coord.rightTop.y, wallHeight,
            coord.leftTop.x, coord.leftTop.y, wallHeight,
            coord.leftBottom.x, coord.leftBottom.y, wallHeight,
            coord.rightBottom.x, coord.rightBottom.y, wallHeight,

            // 0-3-4-5 right
            coord.rightTop.x, coord.rightTop.y, wallHeight,
            coord.rightBottom.x, coord.rightBottom.y, wallHeight,
            coord.rightBottom.x, coord.rightBottom.y, 0f,
            coord.rightTop.x, coord.rightTop.y, 0f,

            // 4-7-6-5 bottom
            coord.rightBottom.x, coord.rightBottom.y, 0f,
            coord.leftBottom.x, coord.leftBottom.y, 0f,
            coord.leftTop.x, coord.leftTop.y, 0f,
            coord.rightTop.x, coord.rightTop.y, 0f,

            //1-6-7-2 left
            coord.leftTop.x, coord.leftTop.y, wallHeight,
            coord.leftTop.x, coord.leftTop.y, 0f,
            coord.leftBottom.x, coord.leftBottom.y, 0f,
            coord.leftBottom.x, coord.leftBottom.y, wallHeight,

            //0-5-6-1 back
            coord.rightTop.x, coord.rightTop.y, wallHeight,
            coord.rightTop.x, coord.rightTop.y, 0f,
            coord.leftTop.x, coord.leftTop.y, 0f,
            coord.leftTop.x, coord.leftTop.y, wallHeight,

            //3-2-7-4 front
            coord.rightBottom.x, coord.rightBottom.y, wallHeight,
            coord.leftBottom.x, coord.leftBottom.y, wallHeight,
            coord.leftBottom.x, coord.leftBottom.y, 0f,
            coord.rightBottom.x, coord.rightBottom.y, 0f
        )

        var textureCoords: FloatArray? = null

        if (mCreateTextureCoords) {
            textureCoords = floatArrayOf(
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, // front
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, // up
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, // back
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, // down
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, // right
                0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f
            )// left
        }

        val colors = floatArrayOf(
            0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,
            1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,
            0.5f, 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f
        )

        val n = 1f

        val normals: FloatArray = floatArrayOf(
            0f, 0f, n, 0f, 0f, n, 0f, 0f, n, 0f, 0f, n, // front
            n, 0f, 0f, n, 0f, 0f, n, 0f, 0f, n, 0f, 0f, // right
            0f, 0f, -n, 0f, 0f, -n, 0f, 0f, -n, 0f, 0f, -n, // back
            -n, 0f, 0f, -n, 0f, 0f, -n, 0f, 0f, -n, 0f, 0f, // left
            0f, n, 0f, 0f, n, 0f, 0f, n, 0f, 0f, n, 0f, // top
            0f, -n, 0f, 0f, -n, 0f, 0f, -n, 0f, 0f, -n, 0f
        )// bottom

        val indices: IntArray? = intArrayOf(
            0, 1, 2,
            0, 2, 3,
            4, 5, 6,
            4, 6, 7,
            8, 9, 10,
            8, 10, 11,
            12, 13, 14,
            12, 14, 15,
            16, 17, 18,
            16, 18, 19,
            20, 21, 22,
            20, 22, 23
        )
        if (!coord.isFloor) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA)
            // 设置透明显示
            GLES20.glEnable(GLES20.GL_ALPHA)
        }

        setData(
            vertices,
            normals,
            textureCoords,
            colors,
            indices,
            true
        )
    }
}
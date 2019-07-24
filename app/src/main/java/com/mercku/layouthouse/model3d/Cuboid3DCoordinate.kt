package com.mercku.layouthouse.model3d

data class Cuboid3DCoordinate(
    val leftTop: Coordinate2D,
    val rightTop: Coordinate2D,
    val leftBottom: Coordinate2D,
    val rightBottom: Coordinate2D,
    val isFloor: Boolean,
    val text: String = ""
)
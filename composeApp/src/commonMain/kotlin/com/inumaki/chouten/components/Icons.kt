package com.inumaki.chouten.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Repo_Regular: ImageVector
    get() {
        if (_Repo_Regular != null) {
            return _Repo_Regular!!
        }
        _Repo_Regular = ImageVector.Builder(
            name = "Repo_Regular",
            defaultWidth = 20.dp,
            defaultHeight = 22.dp,
            viewportWidth = 20f,
            viewportHeight = 22f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFD4D4D4)),
                fillAlpha = 0.7f,
                stroke = null,
                strokeAlpha = 0.7f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(1.38682f, 16.9465f)
                lineTo(9.18276f, 21.3645f)
                curveTo(9.73750f, 21.68150f, 10.26250f, 21.68150f, 10.82710f, 21.36450f)
                lineTo(18.6131f, 16.9465f)
                curveTo(19.52450f, 16.43140f, 200f, 15.90640f, 200f, 14.48980f)
                verticalLineTo(6.64437f)
                curveTo(200f, 5.61420f, 19.62350f, 4.97030f, 18.79150f, 4.49480f)
                lineTo(11.7781f, 0.512629f)
                curveTo(10.57950f, -0.17090f, 9.42050f, -0.17090f, 8.22190f, 0.51260f)
                lineTo(1.21843f, 4.49479f)
                curveTo(0.37640f, 4.97030f, 00f, 5.61420f, 00f, 6.64440f)
                verticalLineTo(14.4898f)
                curveTo(00f, 15.90640f, 0.48540f, 16.43140f, 1.38680f, 16.94650f)
                close()
                moveTo(2.27835f, 15.6389f)
                curveTo(1.70380f, 15.32190f, 1.50570f, 14.98510f, 1.50570f, 14.44030f)
                verticalLineTo(6.96136f)
                lineTo(9.22241f, 11.3695f)
                verticalLineTo(19.5914f)
                lineTo(2.27835f, 15.6389f)
                close()
                moveTo(17.7316f, 15.6389f)
                lineTo(10.7776f, 19.5914f)
                verticalLineTo(11.3695f)
                lineTo(18.4943f, 6.96136f)
                verticalLineTo(14.4403f)
                curveTo(18.49430f, 14.98510f, 18.29610f, 15.32190f, 17.73160f, 15.63890f)
                close()
                moveTo(10.0049f, 9.98267f)
                lineTo(2.3576f, 5.65378f)
                lineTo(5.41852f, 3.90044f)
                lineTo(13.0658f, 8.24911f)
                lineTo(10.0049f, 9.98267f)
                close()
                moveTo(14.6607f, 7.34769f)
                lineTo(6.98364f, 3.00891f)
                lineTo(8.88555f, 1.92917f)
                curveTo(9.64840f, 1.49330f, 10.35160f, 1.48340f, 11.12430f, 1.92920f)
                lineTo(17.6523f, 5.65378f)
                lineTo(14.6607f, 7.34769f)
                close()
            }
        }.build()
        return _Repo_Regular!!
    }

private var _Repo_Regular: ImageVector? = null

val Repo_Solid: ImageVector
    get() {
        if (_Repo_Solid != null) {
            return _Repo_Solid!!
        }
        _Repo_Solid = ImageVector.Builder(
            name = "Repo_Solid",
            defaultWidth = 21.dp,
            defaultHeight = 22.dp,
            viewportWidth = 21f,
            viewportHeight = 22f
        ).apply {
            group {
                path(
                    fill = SolidColor(Color(0xFFD4D4D4)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(10.686f, 21.6758f)
                    curveTo(10.75490f, 21.65610f, 10.81420f, 21.62650f, 10.88310f, 21.58720f)
                    lineTo(18.6012f, 17.1909f)
                    curveTo(19.51790f, 16.66840f, 20.01070f, 16.13620f, 20.01070f, 14.70690f)
                    verticalLineTo(7.0282f)
                    curveTo(20.01070f, 6.73250f, 19.99110f, 6.49590f, 19.93190f, 6.27910f)
                    lineTo(10.686f, 11.5625f)
                    verticalLineTo(21.6758f)
                    close()
                    moveTo(9.33552f, 21.6758f)
                    verticalLineTo(11.5625f)
                    lineTo(0.0895989f, 6.27906f)
                    curveTo(0.03050f, 6.49590f, 0.01070f, 6.73250f, 0.01070f, 7.02820f)
                    verticalLineTo(14.7069f)
                    curveTo(0.01070f, 16.13620f, 0.51350f, 16.66840f, 1.42030f, 17.19090f)
                    lineTo(9.14828f, 21.5872f)
                    curveTo(9.20740f, 21.62650f, 9.26660f, 21.65610f, 9.33550f, 21.67580f)
                    close()
                    moveTo(10.0157f, 10.3796f)
                    lineTo(14.2246f, 7.9942f)
                    lineTo(4.89f, 2.66151f)
                    lineTo(1.27245f, 4.72164f)
                    curveTo(1.05560f, 4.83990f, 0.87820f, 4.95820f, 0.72050f, 5.10610f)
                    lineTo(10.0157f, 10.3796f)
                    close()
                    moveTo(15.5948f, 7.21548f)
                    lineTo(19.3011f, 5.10606f)
                    curveTo(19.15320f, 4.95820f, 18.97580f, 4.83990f, 18.7590f, 4.72160f)
                    lineTo(11.7998f, 0.75909f)
                    curveTo(11.19860f, 0.41410f, 10.59720f, 0.22680f, 10.01570f, 0.22680f)
                    curveTo(9.42430f, 0.22680f, 8.8230f, 0.41410f, 8.22170f, 0.75910f)
                    lineTo(6.22071f, 1.89266f)
                    lineTo(15.5948f, 7.21548f)
                    close()
                }
            }
        }.build()
        return _Repo_Solid!!
    }

private var _Repo_Solid: ImageVector? = null
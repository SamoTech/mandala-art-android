package com.example.generator

import android.graphics.Path
import android.graphics.RectF
import com.example.model.MandalaRegion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MandalaShapes {
    const val CX = 500f
    const val CY = 500f

    fun rad(deg: Double): Float = (deg * PI / 180.0).toFloat()
    fun deg(rad: Float): Float = (rad * 180.0 / PI).toFloat()

    fun createRegionFromPath(id: String, path: Path): MandalaRegion {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return MandalaRegion(id, path, bounds)
    }

    /**
     * Central circle region
     */
    fun circle(id: String, cx: Float = CX, cy: Float = CY, radius: Float): MandalaRegion {
        val p = Path()
        p.addCircle(cx, cy, radius, Path.Direction.CW)
        return createRegionFromPath(id, p)
    }

    /**
     * Circular annular sector (ring segment)
     */
    fun annularSector(
        id: String,
        rInner: Float,
        rOuter: Float,
        startDeg: Float,
        sweepDeg: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        val rectOuter = RectF(cx - rOuter, cy - rOuter, cx + rOuter, cy + rOuter)
        val rectInner = RectF(cx - rInner, cy - rInner, cx + rInner, cy + rInner)

        val a0 = rad(startDeg.toDouble())
        val a1 = rad((startDeg + sweepDeg).toDouble())

        // Start at inner arc start
        p.moveTo(cx + rInner * cos(a0), cy + rInner * sin(a0))
        // Line to outer arc start
        p.lineTo(cx + rOuter * cos(a0), cy + rOuter * sin(a0))
        // Arc along outer circle
        p.arcTo(rectOuter, startDeg, sweepDeg, false)
        // Line to inner arc end
        p.lineTo(cx + rInner * cos(a1), cy + rInner * sin(a1))
        // Arc along inner circle back
        p.arcTo(rectInner, startDeg + sweepDeg, -sweepDeg, false)
        p.close()

        return createRegionFromPath(id, p)
    }

    /**
     * Diamond / Rhombus region
     */
    fun diamond(
        id: String,
        rInner: Float,
        rOuter: Float,
        rMid: Float,
        centerAngleRad: Float,
        halfWidthRad: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        val pInnerX = cx + rInner * cos(centerAngleRad)
        val pInnerY = cy + rInner * sin(centerAngleRad)
        val pRightX = cx + rMid * cos(centerAngleRad + halfWidthRad)
        val pRightY = cy + rMid * sin(centerAngleRad + halfWidthRad)
        val pOuterX = cx + rOuter * cos(centerAngleRad)
        val pOuterY = cy + rOuter * sin(centerAngleRad)
        val pLeftX = cx + rMid * cos(centerAngleRad - halfWidthRad)
        val pLeftY = cy + rMid * sin(centerAngleRad - halfWidthRad)

        p.moveTo(pInnerX, pInnerY)
        p.lineTo(pRightX, pRightY)
        p.lineTo(pOuterX, pOuterY)
        p.lineTo(pLeftX, pLeftY)
        p.close()

        return createRegionFromPath(id, p)
    }

    /**
     * Pointed smooth curved petal
     */
    fun petal(
        id: String,
        rBase: Float,
        rTip: Float,
        centerAngleRad: Float,
        halfWidthRad: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        val baseX = cx + rBase * cos(centerAngleRad)
        val baseY = cy + rBase * sin(centerAngleRad)
        val tipX = cx + rTip * cos(centerAngleRad)
        val tipY = cy + rTip * sin(centerAngleRad)

        val rMid = (rBase + rTip) * 0.5f
        val cp1X = cx + rMid * cos(centerAngleRad - halfWidthRad)
        val cp1Y = cy + rMid * sin(centerAngleRad - halfWidthRad)
        val cp2X = cx + rMid * cos(centerAngleRad + halfWidthRad)
        val cp2Y = cy + rMid * sin(centerAngleRad + halfWidthRad)

        p.moveTo(baseX, baseY)
        p.quadTo(cp1X, cp1Y, tipX, tipY)
        p.quadTo(cp2X, cp2Y, baseX, baseY)
        p.close()

        return createRegionFromPath(id, p)
    }

    /**
     * Triangle ray region
     */
    fun triangleRay(
        id: String,
        rBase: Float,
        rTip: Float,
        startRad: Float,
        tipRad: Float,
        endRad: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        p.moveTo(cx + rBase * cos(startRad), cy + rBase * sin(startRad))
        p.lineTo(cx + rTip * cos(tipRad), cy + rTip * sin(tipRad))
        p.lineTo(cx + rBase * cos(endRad), cy + rBase * sin(endRad))
        p.close()
        return createRegionFromPath(id, p)
    }

    /**
     * Circular bead arranged around ring
     */
    fun bead(
        id: String,
        rDistance: Float,
        radius: Float,
        angleRad: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val bx = cx + rDistance * cos(angleRad)
        val by = cy + rDistance * sin(angleRad)
        return circle(id, bx, by, radius)
    }

    /**
     * Scallop arch region
     */
    fun arch(
        id: String,
        rBase: Float,
        rPeak: Float,
        startDeg: Float,
        sweepDeg: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        val a0 = rad(startDeg.toDouble())
        val a1 = rad((startDeg + sweepDeg).toDouble())
        val aMid = (a0 + a1) * 0.5f

        val p1x = cx + rBase * cos(a0)
        val p1y = cy + rBase * sin(a0)
        val p2x = cx + rBase * cos(a1)
        val p2y = cy + rBase * sin(a1)
        val peakX = cx + rPeak * cos(aMid)
        val peakY = cy + rPeak * sin(aMid)

        p.moveTo(p1x, p1y)
        p.quadTo(peakX, peakY, p2x, p2y)
        // Close along base arc
        val rectInner = RectF(cx - rBase, cy - rBase, cx + rBase, cy + rBase)
        p.arcTo(rectInner, startDeg + sweepDeg, -sweepDeg, false)
        p.close()

        return createRegionFromPath(id, p)
    }

    /**
     * Pinwheel / Spiral spoke
     */
    fun swirlSpoke(
        id: String,
        rInner: Float,
        rOuter: Float,
        startAngleRad: Float,
        twistRad: Float,
        widthRad: Float,
        cx: Float = CX,
        cy: Float = CY
    ): MandalaRegion {
        val p = Path()
        val p1X = cx + rInner * cos(startAngleRad)
        val p1Y = cy + rInner * sin(startAngleRad)

        val p2X = cx + rOuter * cos(startAngleRad + twistRad)
        val p2Y = cy + rOuter * sin(startAngleRad + twistRad)

        val p3X = cx + rOuter * cos(startAngleRad + twistRad + widthRad)
        val p3Y = cy + rOuter * sin(startAngleRad + twistRad + widthRad)

        val p4X = cx + rInner * cos(startAngleRad + widthRad)
        val p4Y = cy + rInner * sin(startAngleRad + widthRad)

        p.moveTo(p1X, p1Y)
        p.quadTo(
            cx + (rInner + rOuter) * 0.5f * cos(startAngleRad + twistRad * 0.3f),
            cy + (rInner + rOuter) * 0.5f * sin(startAngleRad + twistRad * 0.3f),
            p2X, p2Y
        )
        p.lineTo(p3X, p3Y)
        p.quadTo(
            cx + (rInner + rOuter) * 0.5f * cos(startAngleRad + twistRad * 0.3f + widthRad),
            cy + (rInner + rOuter) * 0.5f * sin(startAngleRad + twistRad * 0.3f + widthRad),
            p4X, p4Y
        )
        p.close()

        return createRegionFromPath(id, p)
    }
}

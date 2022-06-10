package org.gaseumlabs.uhc.world.gen

import net.minecraft.util.*
import net.minecraft.world.level.biome.TerrainShaper
import net.minecraft.world.level.biome.TerrainShaper.Point
import org.gaseumlabs.uhc.world.gen.UHCTerrainShaper.UHCCoordinate.*

object UHCTerrainShaper {
	private val NO_TRANSFORM = ToFloatFunction { float: Float -> float }

	private fun getAmplifiedOffset(f: Float): Float {
		return if (f < 0.0f) f else f * 2.0f
	}

	private fun getAmplifiedFactor(f: Float): Float {
		return 1.25f - 6.25f / (f + 5.0f)
	}

	private fun getAmplifiedJaggedness(f: Float): Float {
		return f * 2.0f
	}

	private fun offsetUHC(f: Float): Float {
		return if (f < -0.2f) -0.2f else f
	}

	fun createGame(amplified: Boolean): TerrainShaper {
		/* only do anything if amplified is true */
		val amplifiedOffset =
			if (amplified) ToFloatFunction { getAmplifiedOffset(it) } else ToFloatFunction<Float> { offsetUHC(it) }
		val amplifiedFactor = if (amplified) ToFloatFunction { getAmplifiedFactor(it) } else NO_TRANSFORM
		val amplifiedJaggedness = if (amplified) ToFloatFunction { getAmplifiedJaggedness(it) } else NO_TRANSFORM

		//val cubicSpline = buildErosionOffsetSpline(
		//	-0.15f,
		//	0.0f,
		//	0.0f,
		//	0.1f,
		//	0.0f,
		//	-0.03f,
		//	false,
		//	false,
		//	amplifiedOffset
		//)
		//val cubicSpline2 = buildErosionOffsetSpline(
		//	-0.1f,
		//	0.03f,
		//	0.1f,
		//	0.1f,
		//	0.01f,
		//	-0.03f,
		//	false,
		//	false,
		//	amplifiedOffset
		//)
		//val cubicSpline3 = buildErosionOffsetSpline(
		//	-0.1f,
		//	0.03f,
		//	0.1f,
		//	0.7f,
		//	0.01f,
		//	-0.03f,
		//	true,
		//	true,
		//	amplifiedOffset
		//)
		//val cubicSpline4 = buildErosionOffsetSpline(
		//	-0.05f,
		//	0.03f,
		//	0.1f,
		//	1.0f,
		//	0.01f,
		//	0.01f,
		//	true,
		//	true,
		//	amplifiedOffset
		//)

		val cubicSplineUHC0 = buildErosionOffsetSplineUHC(
			0.00f,
			0.01f,
			0.23f,
			0.02f,
			0.08f,
			1.0f,
			amplifiedOffset
		)
		val cubicSplineUHC1 = buildErosionOffsetSplineUHC(
			0.00f,
			0.01f,
			0.23f,
			0.02f,
			0.08f,
			1.2f,
			amplifiedOffset
		)
		val cubicSplineUHC2 = buildErosionOffsetSplineUHC(
			0.00f,
			0.01f,
			0.23f,
			0.02f,
			0.08f,
			1.1f,
			amplifiedOffset
		)

		val cubicSpline5 = CubicSpline.builder(UHC_CONTINENTS, amplifiedOffset)
			//.addPoint(-1.1f, 0.044f, 0.0f)
			//.addPoint(-1.02f, -0.2222f, 0.0f)
			//.addPoint(-0.51f, -0.2222f, 0.0f)
			//.addPoint(-0.44f, -0.12f, 0.0f)
			//.addPoint(-0.18f, -0.12f, 0.0f)
			//.addPoint(-0.16f, cubicSpline, 0.0f)
			//.addPoint(-0.15f, cubicSpline, 0.0f)
			//.addPoint(-0.1f, cubicSpline2, 0.0f)
			//.addPoint(0.25f, cubicSpline3, 0.0f)
			//.addPoint(1.0f, cubicSpline4, 0.0f)
			.addPoint(0.0f,
				recurSpline(
					simpleSpline(0.03f, 0.01f, -0.06f, 0.00f, 0.26f, amplifiedOffset),
					simpleSpline(0.01f, 0.02f, -0.06f, 0.02f, 0.20f, amplifiedOffset),
					simpleSpline(0.00f, 0.05f, -0.06f, 0.01f, 0.21f, amplifiedOffset),
					simpleSpline(0.02f, 0.03f, -0.06f, 0.03f, 0.18f, amplifiedOffset),
					simpleSpline(0.04f, 0.05f, -0.06f, 0.04f, 0.21f, amplifiedOffset),
					amplifiedOffset,
				),
				0.0f
			)
			//.addPoint(-1.0f, cubicSplineUHC0, 0.0f)
			//.addPoint(0.0f, cubicSplineUHC1, 0.0f)
			//.addPoint(1.0f, cubicSplineUHC2, 0.0f)
			.build()

		val cubicSpline6 = CubicSpline.builder(UHC_CONTINENTS, NO_TRANSFORM)
			.addPoint(-0.19f, 3.95f, 0.0f)
			.addPoint(-0.15f, getErosionFactor(6.25f, true, NO_TRANSFORM), 0.0f)
			.addPoint(-0.1f, getErosionFactor(5.47f, true, amplifiedFactor), 0.0f)
			.addPoint(0.03f, getErosionFactor(5.08f, true, amplifiedFactor), 0.0f)
			.addPoint(0.06f, getErosionFactor(4.69f, false, amplifiedFactor), 0.0f)
			.build()

		val cubicSpline7 = CubicSpline.builder(UHC_CONTINENTS, amplifiedJaggedness)
			.addPoint(-0.11f, 0.0f, 0.0f)
			.addPoint(0.03f, buildErosionJaggednessSpline(1.0f, 0.5f, 0.0f, 0.0f, amplifiedJaggedness), 0.0f)
			.addPoint(0.65f, buildErosionJaggednessSpline(1.0f, 1.0f, 1.0f, 0.0f, amplifiedJaggedness), 0.0f)
			.build()

		return TerrainShaper(cubicSpline5, cubicSpline6, cubicSpline7)
	}

	private fun getErosionFactor(
		value: Float,
		bl: Boolean,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val cubicSpline =
			CubicSpline.builder(UHC_WEIRDNESS, toFloatFunction).addPoint(-0.2f, 6.3f, 0.0f).addPoint(0.2f, value, 0.0f)
				.build()
		val builder = CubicSpline.builder(UHC_EROSION, toFloatFunction).addPoint(-0.6f, cubicSpline, 0.0f)
			.addPoint(-0.5f, CubicSpline.builder(
				UHC_WEIRDNESS, toFloatFunction).addPoint(-0.05f, 6.3f, 0.0f).addPoint(0.05f, 2.67f, 0.0f).build(), 0.0f)
			.addPoint(-0.35f, cubicSpline, 0.0f).addPoint(-0.25f, cubicSpline, 0.0f)
			.addPoint(-0.1f, CubicSpline.builder(
				UHC_WEIRDNESS, toFloatFunction).addPoint(-0.05f, 2.67f, 0.0f).addPoint(0.05f, 6.3f, 0.0f).build(), 0.0f)
			.addPoint(0.03f, cubicSpline, 0.0f)
		if (bl) {
			val cubicSpline2 =
				CubicSpline.builder(UHC_WEIRDNESS, toFloatFunction).addPoint(0.0f, value, 0.0f)
					.addPoint(0.1f, 0.625f, 0.0f)
					.build()
			val cubicSpline3 = CubicSpline.builder(UHC_RIDGES, toFloatFunction).addPoint(-0.9f, value, 0.0f)
				.addPoint(-0.69f, cubicSpline2, 0.0f).build()
			builder.addPoint(0.35f, value, 0.0f).addPoint(0.45f, cubicSpline3, 0.0f).addPoint(0.55f, cubicSpline3, 0.0f)
				.addPoint(0.62f, value, 0.0f)
		} else {
			val cubicSpline4 = CubicSpline.builder(UHC_RIDGES, toFloatFunction).addPoint(-0.7f, cubicSpline, 0.0f)
				.addPoint(-0.15f, 1.37f, 0.0f).build()
			val cubicSpline5 = CubicSpline.builder(UHC_RIDGES, toFloatFunction).addPoint(0.45f, cubicSpline, 0.0f)
				.addPoint(0.7f, 1.56f, 0.0f).build()
			builder.addPoint(0.05f, cubicSpline5, 0.0f).addPoint(0.4f, cubicSpline5, 0.0f)
				.addPoint(0.45f, cubicSpline4, 0.0f).addPoint(0.55f, cubicSpline4, 0.0f).addPoint(0.58f, value, 0.0f)
		}
		return builder.build()
	}

	private fun buildErosionOffsetSplineUHC(
		p0: Float,
		p1: Float,
		p2: Float,
		p3: Float,
		p4: Float,
		mountain: Float,
		amplified: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val builder = CubicSpline.builder(UHC_EROSION, amplified)

		return builder.addPoint(-1.00f, ridgeSpline(p0, p1, p2, p3, p4, 0.5f, amplified), 0.0f)
			.addPoint(-0.75f, ridgeSpline(p0 * 0.7f, p1 * 0.7f, p2 * 0.7f, p3 * 0.7f, p4 * 0.7f, 0.5f, amplified), 0.0f)
			.addPoint(-0.50f, -0.2f, 0.0f) //river
			.addPoint(-0.25f, ridgeSpline(p0, p1, p2, p3, p4, 0.5f, amplified), 0.0f)
			.addPoint(
				0.00f,
				ridgeSpline(p0 * mountain, p1 * mountain, p2 * mountain, p3 * mountain, p4 * mountain, 0.5f, amplified),
				0.0f
			)
			.addPoint(0.25f, ridgeSpline(p0, p1, p2, p3, p4, 0.5f, amplified), 0.0f)
			.addPoint(0.50f, -0.15f, 0.0f) //river
			.addPoint(0.75f, ridgeSpline(p0 * 0.6f, p1 * 0.6f, p2 * 0.6f, p3 * 0.6f, p4 * 0.6f, 0.5f, amplified), 0.0f)
			.addPoint(1.00f, ridgeSpline(p0, p1, p2, p3, p4, 0.5f, amplified), 0.0f)
			.build()
	}

	private fun recurSpline(
		p0: CubicSpline<Point>,
		p1: CubicSpline<Point>,
		p2: CubicSpline<Point>,
		p3: CubicSpline<Point>,
		p4: CubicSpline<Point>,
		amplified: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val builder = CubicSpline.builder(UHC_EROSION, amplified)

		return builder.addPoint(-1.00f, p0, 0.0f)
			.addPoint(-0.50f, p1, 0.0f)
			.addPoint(0.00f, p2, 0.0f)
			.addPoint(0.50f, p3, 0.0f)
			.addPoint(1.00f, p4, 0.0f)
			.build()
	}

	private fun simpleSpline(
		p0: Float,
		p1: Float,
		p2: Float,
		p3: Float,
		p4: Float,
		amplified: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val builder = CubicSpline.builder(UHC_RIDGES, amplified)

		return builder.addPoint(-1.00f, p0, 0.0f)
			.addPoint(-0.25f, p1, 0.0f)
			.addPoint(0.00f, p2, 0.0f)
			.addPoint(0.25f, p3, 0.0f)
			.addPoint(1.00f, p4, 0.0f)
			.build()
	}

	private fun buildErosionOffsetSpline(
		p0: Float,
		p1: Float,
		p2: Float,
		p3: Float,
		p4: Float,
		pSuperLow: Float,
		extraRidge: Boolean,
		extraMountainRidge: Boolean,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point> {

		val mountains0 =
			buildMountainRidgeSplineWithPoints(Mth.lerp(p3, 0.6f, 1.5f), extraMountainRidge, toFloatFunction)
		val mountains1 =
			buildMountainRidgeSplineWithPoints(Mth.lerp(p3, 0.6f, 1.0f), extraMountainRidge, toFloatFunction)
		val mountains2 = buildMountainRidgeSplineWithPoints(p3, extraMountainRidge, toFloatFunction)

		val lowLands = ridgeSpline(p0 - 0.15f, 0.5f * p3, 0.5f * p3, 0.5f * p3, 0.6f * p3, 0.5f, toFloatFunction)
		val cubicSpline5 = ridgeSpline(p0, p4 * p3, p1 * p3, 0.5f * p3, 0.6f * p3, 0.5f, toFloatFunction)
		val cubicSpline6 = ridgeSpline(p0, p4, p4, p1, p2, 0.5f, toFloatFunction)
		val cubicSpline7 = ridgeSpline(p0, p4, p4, p1, p2, 0.5f, toFloatFunction)

		val cubicSpline8 = CubicSpline.builder(UHC_RIDGES, toFloatFunction)
			.addPoint(-1.0f, p0, 0.0f)
			.addPoint(-0.4f, cubicSpline6, 0.0f)
			.addPoint(0.0f, p2 + 0.07f, 0.0f)
			.build()

		val cubicSpline9 = ridgeSpline(-0.02f, pSuperLow, pSuperLow, p1, p2, 0.0f, toFloatFunction)

		val builder = CubicSpline.builder(UHC_EROSION, toFloatFunction)
			.addPoint(-0.85f, mountains0, 0.0f)
			.addPoint(-0.7f, mountains1, 0.0f)
			.addPoint(-0.4f, mountains2, 0.0f)
			.addPoint(-0.35f, lowLands, 0.0f) //-0.35
			.addPoint(-0.30f, cubicSpline5, 0.0f) //-0.1
			.addPoint(0.2f, cubicSpline6, 0.0f)
		if (extraRidge) {
			builder.addPoint(0.4f, cubicSpline7, 0.0f)
				.addPoint(0.45f, cubicSpline8, 0.0f)
				.addPoint(0.55f, cubicSpline8, 0.0f)
				.addPoint(0.58f, cubicSpline7, 0.0f)
		}
		builder.addPoint(0.7f, cubicSpline9, 0.0f)

		return builder.build()
	}

	private fun buildMountainRidgeSplineWithPoints(
		f: Float,
		bl: Boolean,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val builder = CubicSpline.builder(UHC_RIDGES, toFloatFunction)
		val g = -0.7f
		val h = -1.0f
		val i = mountainContinentalness(-1.0f, f, -0.7f)
		val j = 1.0f
		val k = mountainContinentalness(1.0f, f, -0.7f)
		val l = calculateMountainRidgeZeroContinentalnessPoint(f)
		val m = -0.65f
		if (-0.65f < l && l < 1.0f) {
			val n = mountainContinentalness(-0.65f, f, -0.7f)
			val o = -0.75f
			val p = mountainContinentalness(-0.75f, f, -0.7f)
			val q = calculateSlope(i, p, -1.0f, -0.75f)
			builder.addPoint(-1.0f, i, q)
			builder.addPoint(-0.75f, p, 0.0f)
			builder.addPoint(-0.65f, n, 0.0f)
			val r = mountainContinentalness(l, f, -0.7f)
			val s = calculateSlope(r, k, l, 1.0f)
			val t = 0.01f
			builder.addPoint(l - 0.01f, r, 0.0f)
			builder.addPoint(l, r, s)
			builder.addPoint(1.0f, k, s)
		} else {
			val u = calculateSlope(i, k, -1.0f, 1.0f)
			if (bl) {
				builder.addPoint(-1.0f, 0.2f.coerceAtLeast(i), 0.0f)
				builder.addPoint(0.0f, Mth.lerp(0.5f, i, k), u)
			} else {
				builder.addPoint(-1.0f, i, u)
			}
			builder.addPoint(1.0f, k, u)
		}
		return builder.build()
	}

	private fun ridgeSpline(
		p0: Float,
		p1: Float,
		p2: Float,
		p3: Float,
		p4: Float,
		minLowDerivative: Float,
		amplified: ToFloatFunction<Float>,
	): CubicSpline<Point?> {
		val l = (0.5f * (p1 - p0)).coerceAtLeast(minLowDerivative)
		val m = 5.0f * (p2 - p1)

		return CubicSpline.builder(UHC_RIDGES, amplified)
			.addPoint(-1.0f, p0, l)
			.addPoint(-0.4f, p1, l.coerceAtMost(m))
			.addPoint(0.0f, p2, m)
			.addPoint(0.4f, p3, 2.0f * (p3 - p2))
			.addPoint(1.0f, p4, 0.7f * (p4 - p3))
			.build()
	}

	private fun mountainContinentalness(weirdness: Float, continentalness: Float, weirdnessThreshold: Float): Float {
		val f = 1.17f
		val g = 0.46082947f
		val h = 1.0f - (1.0f - continentalness) * 0.5f
		val i = 0.5f * (1.0f - continentalness)
		val j = (weirdness + 1.17f) * 0.46082947f
		val k = j * h - i
		return if (weirdness < weirdnessThreshold) k.coerceAtLeast(-0.2222f) else k.coerceAtLeast(0.0f)
	}

	private fun calculateMountainRidgeZeroContinentalnessPoint(continentalness: Float): Float {
		val f = 1.17f
		val g = 0.46082947f
		val h = 1.0f - (1.0f - continentalness) * 0.5f
		val i = 0.5f * (1.0f - continentalness)
		return i / (0.46082947f * h) - 1.17f
	}

	private fun calculateSlope(f: Float, g: Float, h: Float, i: Float): Float {
		return (g - f) / (i - h)
	}

	private fun buildErosionJaggednessSpline(
		f: Float,
		g: Float,
		h: Float,
		i: Float,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point?> {
		val j = -0.5775f
		val cubicSpline = buildRidgeJaggednessSpline(f, h, toFloatFunction)
		val cubicSpline2 = buildRidgeJaggednessSpline(g, i, toFloatFunction)
		return CubicSpline.builder(UHC_EROSION, toFloatFunction).addPoint(-1.0f, cubicSpline, 0.0f)
			.addPoint(-0.78f, cubicSpline2, 0.0f).addPoint(-0.5775f, cubicSpline2, 0.0f).addPoint(-0.375f, 0.0f, 0.0f)
			.build()
	}

	private fun buildRidgeJaggednessSpline(
		f: Float,
		g: Float,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val h = TerrainShaper.peaksAndValleys(0.4f)
		val i = TerrainShaper.peaksAndValleys(0.56666666f)
		val j = (h + i) / 2.0f
		val builder = CubicSpline.builder(UHC_RIDGES, toFloatFunction)
		builder.addPoint(h, 0.0f, 0.0f)
		if (g > 0.0f) {
			builder.addPoint(j, buildWeirdnessJaggednessSpline(g, toFloatFunction), 0.0f)
		} else {
			builder.addPoint(j, 0.0f, 0.0f)
		}
		if (f > 0.0f) {
			builder.addPoint(1.0f, buildWeirdnessJaggednessSpline(f, toFloatFunction), 0.0f)
		} else {
			builder.addPoint(1.0f, 0.0f, 0.0f)
		}
		return builder.build()
	}

	private fun buildWeirdnessJaggednessSpline(
		f: Float,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point?> {
		val g = 0.63f * f
		val h = 0.3f * f
		return CubicSpline.builder(UHC_WEIRDNESS, toFloatFunction).addPoint(-0.01f, g, 0.0f).addPoint(0.01f, h, 0.0f)
			.build()
	}

	enum class UHCCoordinate(
		val noiseFunction: ToFloatFunction<Point>,
	) : ToFloatFunction<Point> {
		UHC_CONTINENTS(Point::continents),
		UHC_EROSION(Point::erosion),
		UHC_WEIRDNESS(Point::weirdness),
		UHC_RIDGES(Point::ridges);

		override fun apply(point: Point): Float {
			return noiseFunction.apply(point)
		}
	}
}
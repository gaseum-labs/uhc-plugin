package com.codeland.uhc.world.gen.climate

import com.codeland.uhc.world.gen.climate.UHCTerrainShaper.UHCCoordinate.*
import net.minecraft.util.*
import net.minecraft.world.level.biome.TerrainShaper
import net.minecraft.world.level.biome.TerrainShaper.Point

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

	fun createGame(amplified: Boolean): TerrainShaper {
		val toFloatFunction = if (amplified) ToFloatFunction { getAmplifiedOffset(it) } else NO_TRANSFORM
		val toFloatFunction2 = if (amplified) ToFloatFunction { getAmplifiedFactor(it) } else NO_TRANSFORM
		val toFloatFunction3 = if (amplified) ToFloatFunction { getAmplifiedJaggedness(it) } else NO_TRANSFORM

		val cubicSpline = buildErosionOffsetSpline(-0.15f,
			0.0f,
			0.0f,
			0.1f,
			0.0f,
			-0.03f,
			false,
			false,
			toFloatFunction
		)
		val cubicSpline2 = buildErosionOffsetSpline(-0.1f,
			0.03f,
			0.1f,
			0.1f,
			0.01f,
			-0.03f,
			false,
			false,
			toFloatFunction
		)
		val cubicSpline3 =
			buildErosionOffsetSpline(-0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, toFloatFunction)
		val cubicSpline4 =
			buildErosionOffsetSpline(-0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, toFloatFunction)
		val f = -0.51f
		val g = -0.4f
		val h = 0.1f
		val i = -0.15f
		//	.addPoint(-1.1f, 0.044f, 0.0f)
		//	.addPoint(-1.02f, -0.2222f, 0.0f)
		//	.addPoint(-0.51f, -0.2222f, 0.0f)
		//	.addPoint(-0.44f, -0.12f, 0.0f)
		//	.addPoint(-0.18f, -0.12f, 0.0f)
		//	.addPoint(-0.16f, cubicSpline, 0.0f)
		//	.addPoint(-0.15f, cubicSpline, 0.0f)
		//	.addPoint(-0.1f, cubicSpline2, 0.0f)
		//	.addPoint(0.25f, cubicSpline3, 0.0f)
		//	.addPoint(1.0f, cubicSpline4, 0.0f)
		//	.build()
		val cubicSpline5 = CubicSpline.builder(UHC_CONTINENTS, toFloatFunction)
			.addPoint(-1.00f, cubicSpline, 0.0f)
			.addPoint(-0.50f, cubicSpline, 0.0f)
			.addPoint(-0.00f, cubicSpline2, 0.0f)
			.addPoint(0.50f, cubicSpline3, 0.0f)
			.addPoint(1.00f, cubicSpline4, 0.0f)
			.build()
		val cubicSpline6 = CubicSpline.builder(UHC_CONTINENTS, NO_TRANSFORM).addPoint(-0.19f, 3.95f, 0.0f)
			.addPoint(-0.15f, getErosionFactor(6.25f, true, NO_TRANSFORM), 0.0f)
			.addPoint(-0.1f, getErosionFactor(5.47f, true, toFloatFunction2), 0.0f)
			.addPoint(0.03f, getErosionFactor(5.08f, true, toFloatFunction2), 0.0f)
			.addPoint(0.06f, getErosionFactor(4.69f, false, toFloatFunction2), 0.0f).build()
		val j = 0.65f
		val cubicSpline7 = CubicSpline.builder(UHC_CONTINENTS, toFloatFunction3).addPoint(-0.11f, 0.0f, 0.0f)
			.addPoint(0.03f, buildErosionJaggednessSpline(1.0f, 0.5f, 0.0f, 0.0f, toFloatFunction3), 0.0f)
			.addPoint(0.65f, buildErosionJaggednessSpline(1.0f, 1.0f, 1.0f, 0.0f, toFloatFunction3), 0.0f)
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

	private fun buildErosionOffsetSpline(
		f: Float,
		g: Float,
		h: Float,
		i: Float,
		j: Float,
		k: Float,
		bl: Boolean,
		bl2: Boolean,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point> {
		val l = 0.6f
		val m = 0.5f
		val n = 0.5f
		val cubicSpline =
			buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6f, 1.5f), bl2, toFloatFunction)
		val cubicSpline2 =
			buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6f, 1.0f), bl2, toFloatFunction)
		val cubicSpline3 = buildMountainRidgeSplineWithPoints(i, bl2, toFloatFunction)
		val cubicSpline4 = ridgeSpline(f - 0.15f,
			0.5f * i,
			Mth.lerp(0.5f, 0.5f, 0.5f) * i,
			0.5f * i,
			0.6f * i,
			0.5f,
			toFloatFunction)
		val cubicSpline5 = ridgeSpline(f, j * i, g * i, 0.5f * i, 0.6f * i, 0.5f, toFloatFunction)
		val cubicSpline6 = ridgeSpline(f, j, j, g, h, 0.5f, toFloatFunction)
		val cubicSpline7 = ridgeSpline(f, j, j, g, h, 0.5f, toFloatFunction)
		val cubicSpline8 =
			CubicSpline.builder(UHC_RIDGES, toFloatFunction).addPoint(-1.0f, f, 0.0f)
				.addPoint(-0.4f, cubicSpline6, 0.0f)
				.addPoint(0.0f, h + 0.07f, 0.0f).build()
		val cubicSpline9 = ridgeSpline(-0.02f, k, k, g, h, 0.0f, toFloatFunction)
		val builder = CubicSpline.builder(UHC_EROSION, toFloatFunction).addPoint(-0.85f, cubicSpline, 0.0f)
			.addPoint(-0.7f, cubicSpline2, 0.0f).addPoint(-0.4f, cubicSpline3, 0.0f)
			.addPoint(-0.35f, cubicSpline4, 0.0f).addPoint(-0.1f, cubicSpline5, 0.0f).addPoint(0.2f, cubicSpline6, 0.0f)
		if (bl) {
			builder.addPoint(0.4f, cubicSpline7, 0.0f).addPoint(0.45f, cubicSpline8, 0.0f)
				.addPoint(0.55f, cubicSpline8, 0.0f).addPoint(0.58f, cubicSpline7, 0.0f)
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
		f: Float,
		g: Float,
		h: Float,
		i: Float,
		j: Float,
		k: Float,
		toFloatFunction: ToFloatFunction<Float>,
	): CubicSpline<Point?> {
		val l = (0.5f * (g - f)).coerceAtLeast(k)
		val m = 5.0f * (h - g)
		return CubicSpline.builder(UHC_RIDGES, toFloatFunction)
			.addPoint(-1.0f, f, l)
			.addPoint(-0.4f, g, l.coerceAtMost(m))
			.addPoint(0.0f, h, m)
			.addPoint(0.4f, i, 2.0f * (i - h))
			.addPoint(1.0f, j, 0.7f * (j - i))
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
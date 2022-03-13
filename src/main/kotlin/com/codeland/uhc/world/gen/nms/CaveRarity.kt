package net.minecraft.world.level.levelgen

import com.codeland.uhc.reflect.UHCReflect
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction
import net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler.RarityValueMapper

object CaveRarity {
	val mapperField = RarityValueMapper.TYPE1::class.java.getDeclaredField(
		UHCReflect.remapper.remapFieldName(
			RarityValueMapper.TYPE1::class.java, "mapper"
		)
	)

	val maxRarityField = RarityValueMapper.TYPE1::class.java.getDeclaredField(
		UHCReflect.remapper.remapFieldName(
			RarityValueMapper.TYPE1::class.java, "maxRarity"
		)
	)

	init {
		mapperField.isAccessible = true
		maxRarityField.isAccessible = true
	}

	internal fun inject() {
		mapperField[RarityValueMapper.TYPE1] = Double2DoubleFunction { 0.75 }
		maxRarityField[RarityValueMapper.TYPE1] = 0.75

		mapperField[RarityValueMapper.TYPE2] = Double2DoubleFunction { 0.75 }
		maxRarityField[RarityValueMapper.TYPE2] = 0.75
	}
}
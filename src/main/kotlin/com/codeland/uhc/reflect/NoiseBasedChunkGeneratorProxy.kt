package com.codeland.uhc.reflect

import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldSetter
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies

@Proxies(NoiseBasedChunkGenerator::class)
private interface NoiseBasedChunkGeneratorProxy {

	@FieldSetter("sampler")
	fun setSampler(instance: NoiseBasedChunkGenerator, value: Climate.Sampler)
}

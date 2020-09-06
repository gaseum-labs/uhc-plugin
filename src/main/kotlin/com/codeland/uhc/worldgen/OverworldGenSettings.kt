package com.codeland.uhc.worldgen

import nl.rutgerkok.worldgeneratorapi.WorldRef
import org.bukkit.plugin.Plugin
import java.util.*

class OverworldGenSettings(plugin: Plugin?, world: WorldRef?) {
	var world: WorldRef? = null
	var worldSeed: Long
	var seaLevel: Int
	var baseHeight: Float
	var baseSize: Float
	var biomeDepthOffset: Float
	var biomeDepthWeight: Float
	var biomeScaleOffset: Float
	var biomeScaleWeight: Float
	var coordinateScale: Float
	var depthNoiseScaleExponent: Float
	var depthNoiseScaleX: Float
	var depthNoiseScaleZ: Float
	var heightScale: Float
	var heightVariation: Float
	var lowerLimitScale: Float
	var lowerLimitScaleWeight: Float
	var mainNoiseScaleX: Float
	var mainNoiseScaleY: Float
	var mainNoiseScaleZ: Float
	var stretchY: Float
	var upperLimitScale: Float
	var upperLimitScaleWeight: Float

	init {
		var world = world
		world = world
		worldSeed = Random().nextLong()
		baseHeight = 0.1f
		baseSize = 8.5f
		biomeDepthOffset = 0f
		biomeDepthWeight = 1f
		biomeScaleOffset = 0f
		biomeScaleWeight = 1f
		coordinateScale = 684.412f
		depthNoiseScaleExponent = 0.5f
		depthNoiseScaleX = 200f
		depthNoiseScaleZ = 200f
		heightScale = 684.412f
		heightVariation = 0.1f
		lowerLimitScale = 512f
		lowerLimitScaleWeight = 0f
		mainNoiseScaleX = 80f
		mainNoiseScaleY = 160f
		mainNoiseScaleZ = 80f
		seaLevel = 63
		stretchY = 12f
		upperLimitScale = 512f
		upperLimitScaleWeight = 1.2f
	}
}
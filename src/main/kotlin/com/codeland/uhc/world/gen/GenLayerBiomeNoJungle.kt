import net.minecraft.server.v1_16_R3.AreaTransformer5
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerBiomeNoJungle(moist: Boolean) : AreaTransformer5 {
	private val f = if (moist) a else b

	companion object {
		private val a = intArrayOf(1, 2, 4, 3, 5, 6, 38, 39)
		private val b = intArrayOf(1, 2, 35, 38, 39)
		private val c = intArrayOf(1, 3, 4, 6, 27, 29)
		private val d = intArrayOf(1, 3, 4, 5, 32)
		private val e = intArrayOf(12, 30)
	}

	override fun a(var0: WorldGenContext, incoming: Int): Int {
		val incomingBiome = incoming and (0x0f00.inv())

		return if (incomingBiome != 14) {
			when (incomingBiome) {
				1 -> f[var0.a(f.size)]
				2 -> c[var0.a(c.size)]
				3 -> d[var0.a(d.size)]
				4 -> e[var0.a(e.size)]
				else -> 14
			}
		} else {
			incomingBiome
		}
	}
}

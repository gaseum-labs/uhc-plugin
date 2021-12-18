package com.codeland.uhc.quirk.quirks.classes

import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

object Scorch {
    val commonSubs: List<Pair<List<Material>, List<Material>>> = listOf(
        listOf(Material.STONE) to listOf(Material.BLACKSTONE, Material.BLACKSTONE, Material.MAGMA_BLOCK),
        listOf(Material.WATER) to listOf(Material.LAVA),
        listOf(Material.SAND, Material.GRAVEL, Material.SANDSTONE, Material.RED_SAND, Material.RED_SANDSTONE)
                to listOf(Material.SOUL_SAND, Material.SOUL_SAND, Material.MAGMA_BLOCK),
    )

    val warpedSubs: List<Pair<List<Material>, List<Material>>> = listOf(
        Util.materialRange(Material.OAK_LOG, Material.STRIPPED_DARK_OAK_WOOD)
                to listOf(Material.WARPED_STEM),

        Util.materialRange(Material.OAK_PLANKS, Material.WARPED_PLANKS)
                to listOf(Material.WARPED_PLANKS),

        listOf(Material.GRASS_BLOCK, Material.PODZOL, Material.COARSE_DIRT)
                to listOf(Material.WARPED_NYLIUM, Material.WARPED_NYLIUM, Material.MAGMA_BLOCK),

        listOf(Material.DIRT) to listOf(Material.NETHERRACK),

        Util.materialRange(Material.OAK_LEAVES, Material.DARK_OAK_LEAVES)
                to listOf(Material.WARPED_WART_BLOCK),

        Util.materialRange(Material.GRASS, Material.SEAGRASS).plus(Util.materialRange(Material.SUNFLOWER, Material.LARGE_FERN))
                to listOf(Material.WARPED_FUNGUS),
    )

    val crimsonSubs: List<Pair<List<Material>, List<Material>>> = listOf(
        Util.materialRange(Material.OAK_LOG, Material.STRIPPED_DARK_OAK_WOOD)
                to listOf(Material.CRIMSON_STEM),

        Util.materialRange(Material.OAK_PLANKS, Material.WARPED_PLANKS)
                to listOf(Material.CRIMSON_PLANKS),

        listOf(Material.GRASS_BLOCK, Material.PODZOL, Material.COARSE_DIRT)
                to listOf(Material.CRIMSON_NYLIUM, Material.CRIMSON_NYLIUM, Material.MAGMA_BLOCK),

        listOf(Material.DIRT) to listOf(Material.NETHERRACK),

        Util.materialRange(Material.OAK_LEAVES, Material.DARK_OAK_LEAVES)
                to listOf(Material.NETHER_WART_BLOCK),

        Util.materialRange(Material.GRASS, Material.SEAGRASS)
                to listOf(Material.CRIMSON_FUNGUS),
    )

    val blackstoneSubs: List<Pair<List<Material>, List<Material>>> = listOf(
        Util.materialRange(Material.OAK_LOG, Material.STRIPPED_DARK_OAK_WOOD)
                to listOf(Material.POLISHED_BASALT),

        listOf(Material.GRASS_BLOCK, Material.PODZOL, Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT)
                to listOf(Material.BASALT, Material.BASALT, Material.MAGMA_BLOCK),

        Util.materialRange(Material.OAK_LEAVES, Material.DARK_OAK_LEAVES)
                to listOf(Material.SMOOTH_BASALT),

        Util.materialRange(Material.GRASS, Material.SEAGRASS)
                to listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
    )

    val mobSubs: List<Pair<List<EntityType>, List<EntityType>>> = listOf(
        listOf(EntityType.PIG) to listOf(EntityType.ZOMBIFIED_PIGLIN),
        listOf(EntityType.SHEEP, EntityType.COW, EntityType.HORSE, EntityType.LLAMA)
                to listOf(EntityType.HOGLIN),
        listOf(EntityType.CHICKEN, EntityType.RABBIT, EntityType.FOX)
                to listOf(EntityType.MAGMA_CUBE),
        listOf(EntityType.SQUID, EntityType.DOLPHIN)
                to listOf(EntityType.STRIDER),
        listOf(EntityType.SKELETON) to listOf(EntityType.WITHER_SKELETON),
        listOf(EntityType.ZOMBIE) to listOf(EntityType.BLAZE),
    )

    fun scorch(player: Player) {
        val subs = listOf(
            warpedSubs,
            crimsonSubs,
            blackstoneSubs,
        ).random()
        val location = player.location.clone()
        val blockPartition: MutableMap<Int, MutableList<Pair<Int, Int>>> = mutableMapOf()
        val mobPartition: MutableMap<Int, MutableList<Entity>> = mutableMapOf()
        val radius = 15
        val mobs = player.location.world.getNearbyEntities(location, radius.toDouble(), radius.toDouble(), radius.toDouble())
        for (x in -radius..radius) for (z in -radius..radius) {
            blockPartition.getOrPut(sqrt(x * x + z * z.toDouble()).roundToInt()) { mutableListOf() }.add(x to z)
        }
        for (mob in mobs) {
            val relativeLocation = mob.location.subtract(location)
            mobPartition.getOrPut(sqrt(relativeLocation.x * relativeLocation.x + relativeLocation.z * relativeLocation.z).roundToInt()) { mutableListOf() }.add(mob)
        }
        SchedulerUtil.delayedFor(2, blockPartition.keys.sorted().filter { it <= radius }) { r ->
            fun replaceBlocks(pair: Pair<Int, Int>) {
                val (x, z) = pair
                for (y in 255 downTo 0) {
                    val block = player.world.getBlockAt(x, y, z)
                    val sub = subs.find { it.first.contains(block.type) }?.second?.random()
                        ?: commonSubs.find { it.first.contains(block.type) }?.second?.random()
                    if (sub != null) {
                        block.setType(sub, false)
                    }
                }
                if (Random.nextDouble() < 0.03) {
                    player.world
                        .getBlockAt(x, Util.topBlockY(player.world, x, z) + 1, z)
                        .setType(Material.FIRE, false)
                }
            }
            fun transformMob(entity: Entity) {
                val type = entity.type
                val sub = mobSubs.find { it.first.contains(type) }?.second?.random()
                if (sub != null) {
                    val newEntity = location.world.spawnEntity(entity.location, sub)
                    newEntity.location.direction = entity.location.direction
                    entity.remove()
                }
            }
            blockPartition[r]!!
                .map { (x, z) -> Pair(location.blockX + x, location.blockZ + z) }
                .forEach(::replaceBlocks)
            mobPartition[r]
                ?.forEach(::transformMob)
        }
    }
}
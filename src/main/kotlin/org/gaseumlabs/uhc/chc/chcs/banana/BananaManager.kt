package org.gaseumlabs.uhc.chc.chcs.banana

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import org.gaseumlabs.uhc.component.ComponentAction.uhcMessage
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.plus
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.times

object BananaManager : Listener {

    fun tick() {
        bananarangTick()
        bananaPhoneTick()
    }

    fun sendBananaMessage(player: Player, message: String) {
        player.uhcMessage(
                UHCComponent.text(message, UHCColor.BANANA)
        )
    }

    // BANANARANGS
    data class Bananarang(
            val thrower: Player,
            val location: Location,
            val velocity: Vector,
            val stand: ArmorStand,
            val hit: MutableList<Entity> = mutableListOf(),
            val time: Long = System.currentTimeMillis(),
            var rotation: Float = 0f
    )

    val bananarangs: MutableList<Bananarang> = mutableListOf()

    fun createBananarang(player: Player) {
        val armorStand = player.world.spawnEntity(player.location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isMarker = true
        armorStand.isInvulnerable = true
        armorStand.isInvisible = true
        armorStand.equipment.setItemInMainHand(ItemStack(Material.GOLDEN_PICKAXE))
        armorStand.rightArmPose = EulerAngle(Math.PI / 2, 0.0, Math.PI / 2)

        val bananarang = Bananarang(player, player.location.clone(), player.location.direction.clone().setY(0.0).normalize(), armorStand)
        bananarangs.add(bananarang)
    }

    private fun bananarangTick() {
        bananarangs.forEach { bananarang ->
            bananarang.velocity.rotateAroundY(2 * Math.PI / (5 * 20))
            bananarang.location.add(bananarang.velocity)
            bananarang.stand.teleport(bananarang.location)
            bananarang.stand.setRotation(bananarang.rotation, 0f)
            bananarang.rotation += (2 * Math.PI / (20 / 2)).toFloat()
            bananarang.stand.setRotation(0.0f, bananarang.rotation)
            bananarang.stand
                .getNearbyEntities(2.0, 2.0, 2.0)
                .filterIsInstance<LivingEntity>()
                .filter { it !in bananarang.hit }
                .forEach {
                    if (it === bananarang.thrower) {
                        if (System.currentTimeMillis() - bananarang.time > 1000) {
                            bananarangs.remove(bananarang)
                            bananarang.thrower.inventory.addItem(
                                    BANANARANG.create()
                            )
                            bananarang.stand.remove()
                        }
                    } else {
                       bananarang.hit += it
                        it.damage(4.0)
                        it.velocity += bananarang.velocity * 0.5
                    }
                }
        }
    }

    // ROCKET BANANA
    val launched: MutableList<Player> = mutableListOf()

    @EventHandler
    fun fallDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return

        if (player in launched && event.cause == EntityDamageEvent.DamageCause.FALL) {
            launched.remove(player)
            event.isCancelled = true
        }
    }

    // BANANA PHONE
    data class BananaPhoneInstance(
            val initiator: Player,
            var receiver: Player?,
            val answered: Boolean = false,
            var lastRing: Long = System.currentTimeMillis(),
            val callStart: Long = System.currentTimeMillis()
    )

    private val bpInstances: MutableList<BananaPhoneInstance> = mutableListOf()

    private fun getNewBananaPhoneCandidates(): List<Player> {
        return PlayerData.playerDataList
                .filterValues { it.participating && it.alive }
                .keys.mapNotNull(Bukkit::getPlayer)
                .filter { player ->
                    player.inventory.contains(BANANA_PHONE.create())
                            && bpInstances.none { it.initiator === player || it.receiver === player }
                }
    }

    fun onBananaPhoneRightClick(player: Player) {
        val initiatorInstance = bpInstances.find { it.initiator === player }
        if (initiatorInstance != null) {
            sendBananaMessage(player, "You hung up.")
            if (initiatorInstance.answered) {
                sendBananaMessage(initiatorInstance.receiver!!, "They hung up.")
            }
            transferPlayersFromBananaVC(initiatorInstance)
            bpInstances.remove(initiatorInstance)
            return
        }
        val receiverInstance = bpInstances.find { it.receiver === player }
        if (receiverInstance != null) {
            if (receiverInstance.answered) {
                sendBananaMessage(player, "You hung up.")
                transferPlayersFromBananaVC(receiverInstance)
                bpInstances.remove(receiverInstance)
            } else {
                sendBananaMessage(player, "You answered the phone.")
                transferPlayersToBananaVC(receiverInstance)
            }
            return
        }

        // this is a new banana call

        sendBananaMessage(player, "Ringing...")
        val newInstance = BananaPhoneInstance(player, null)
        bpInstances += newInstance
        newInstance.receiver = getNewBananaPhoneCandidates().randomOrNull()?.also {
            sendBananaMessage(it, "You're getting a call on your banana phone. Right click to answer.")
        }
    }

    private fun bananaPhoneTick() {
        bpInstances.filter { (System.currentTimeMillis() - it.callStart) > 5000 }.forEach { instance ->
            bpInstances.remove(instance)
            sendBananaMessage(instance.initiator, "No one picked up.")
        }
        bpInstances.filter { (System.currentTimeMillis() - it.lastRing) > 3000 && !it.answered }.forEach { instance ->
            instance.lastRing = System.currentTimeMillis()
            if (instance.receiver != null) {
                SchedulerUtil.delayedFor(2, 1..3) {
                    instance.receiver!!.world.playSound(
                            instance.receiver!!.location,
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            1.0f,
                            1.0f
                    )
                }
            }
        }
    }

    private fun transferPlayersToBananaVC(instance: BananaPhoneInstance) {

    }

    private fun transferPlayersFromBananaVC(instance: BananaPhoneInstance) {

    }
}
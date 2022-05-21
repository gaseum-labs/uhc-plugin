package org.gaseumlabs.uhc.core

import net.minecraft.network.chat.TextComponent
import net.minecraft.world.BossEvent
import java.util.*

class UHCBossEvent(uuid: UUID, name: TextComponent, color: BossBarColor, style: BossBarOverlay) :
	BossEvent(uuid, name, color, style)
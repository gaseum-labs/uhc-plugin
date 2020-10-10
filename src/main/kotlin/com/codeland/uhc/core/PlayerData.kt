package com.codeland.uhc.core

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class PlayerData(var participating: Boolean, var alive: Boolean, var optingOut: Boolean) {
	var actionsQueue: Queue<(Player) -> Unit> = LinkedList()
}

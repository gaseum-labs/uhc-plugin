package org.gaseumlabs.uhc.util

import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType

object WorldStorage {
	fun <T, Z : Any>setData(
		world: World,
		key: NamespacedKey,
		dataType: PersistentDataType<T, Z>,
		value: Z
	) {
		world.persistentDataContainer.set(key, dataType, value)
	}

	fun <T, Z : Any>getData(
		world: World,
		key: NamespacedKey,
		dataType: PersistentDataType<T, Z>,
	): Z? {
		return world.persistentDataContainer.get(key, dataType)
	}
}

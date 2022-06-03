package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.database.summary.GameType
import org.gaseumlabs.uhc.gui.*
import org.gaseumlabs.uhc.gui.guiItem.*
import org.gaseumlabs.uhc.gui.guiItem.impl.CloseButton
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CreateGameGui(val gameConfig: GameConfig) : GuiPage(
	3,
	Util.gradientString("Create Game", TextColor.color(0x0d5b61), TextColor.color(0x093c80)),
	GuiType.CREATE_GAME
) {
	val quirkGui = GuiManager.register(QuirkGui(gameConfig, this))
	val worldGenGui = GuiManager.register(WorldGenGui(gameConfig, this))

	init {
		/* row 1 */

		addItem(object : GuiItemToggle(coords(0, 0), gameConfig.naturalRegeneration) {
			override fun getStackProperty(value: Boolean): ItemStack {
				return ItemCreator.fromType(
					if (value) Material.BEEF else Material.ROTTEN_FLESH
				).lore(
					Component.text("Is natural regeneration allowed after grace?")
				).name(
					ItemCreator.enabledName("Natural Regeneration", value)
				).create()
			}
		})
		addItem(object : GuiItemCycler<KillReward>(coords(1, 0), gameConfig.killReward, KillReward.values()) {
			override fun getStackProperty(value: KillReward): ItemStack {
				return ItemCreator.fromType(value.representation)
					.name(ItemCreator.stateName("Kill Reward", value.prettyName))
					.lore(value.lore)
					.create()
			}
		})
		addItem(object : GuiItemCycler<World.Environment>(coords(2, 0),
			gameConfig.defaultWorldEnvironment,
			arrayOf(World.Environment.NORMAL, World.Environment.NETHER)) {
			override fun getStackProperty(value: World.Environment): ItemStack {
				return ItemCreator.fromType(
					if (value === World.Environment.NORMAL) Material.GRASS_BLOCK
					else Material.NETHERRACK
				)
					.name(ItemCreator.stateName("World",
						if (value === World.Environment.NORMAL) "Normal"
						else "Nether"
					))
					.lore(Component.text("Which dimension the UHC starts in"))
					.create()
			}

		})
		addItem(object : GuiItemToggle(coords(3, 0), gameConfig.usingBot) {
			override fun getStackProperty(value: Boolean): ItemStack {
				return ItemCreator.fromType(
					if (UHC.bot == null) Material.GUNPOWDER
					else if (value) Material.NAUTILUS_SHELL
					else Material.HONEYCOMB
				).lore(Component.text("Separate teams into separate discord vcs?")).name(
					if (UHC.bot == null) Component.text("Bot is not running", NamedTextColor.RED, TextDecoration.BOLD)
					else ItemCreator.enabledName("Bot VCs", value)
				).create()
			}
		})
		addItem(object :
			GuiItemCycler<GameType>(coords(4, 0), gameConfig.gameType, arrayOf(GameType.UHC, GameType.CHC)) {
			override fun getStackProperty(value: GameType): ItemStack {
				return ItemCreator.fromType(value.representation)
					.name(ItemCreator.stateName("Game Type", value.name))
					.lore(Component.text("For record keeping purposes"))
					.create()
			}
		})

		/* row 2 */

		addItem(object : GuiItemCounterF(coords(0, 1), gameConfig.scale, 0.5f, 6.0f, 0.1f) {
			override fun getStackProperty(value: Float): ItemStack {
				return ItemCreator.fromType(Material.WARPED_TRAPDOOR)
					.enchant(value != property.default)
					.name(ItemCreator.stateName("Scale", "%.2f".format(value)))
					.create()
			}
		})
		addItem(object : GuiItemCounter(coords(1, 1), gameConfig.endgameRadius, 10, 80, 5) {
			override fun getStackProperty(value: Int): ItemStack {
				return ItemCreator.fromType(Material.WARPED_DOOR)
					.enchant(value != property.default)
					.name(ItemCreator.stateName("Endgame Radius", "$value"))
					.create()
			}
		})
		addItem(object : GuiItemCounter(coords(2, 1), gameConfig.graceTime, 60, 2400, 60) {
			override fun getStackProperty(value: Int): ItemStack {
				return ItemCreator.fromType(Material.SUNFLOWER)
					.enchant(value != property.default)
					.name(ItemCreator.stateName("Grace Time", Util.timeString(value)))
					.create()
			}
		})
		addItem(object : GuiItemCounter(coords(3, 1), gameConfig.shrinkTime, 60, 6000, 60) {
			override fun getStackProperty(value: Int): ItemStack {
				return ItemCreator.fromType(Material.BELL)
					.enchant(value != property.default)
					.name(ItemCreator.stateName("Shrink Time", Util.timeString(value)))
					.create()
			}
		})
		addItem(object : GuiItemCounter(coords(4, 1), gameConfig.collapseTime, 100, 1000, 30) {
			override fun getStackProperty(value: Int): ItemStack {
				return ItemCreator.fromType(Material.HORN_CORAL)
					.enchant(value != property.default)
					.name(ItemCreator.stateName("Collapse Time", Util.timeString(value)))
					.create()
			}
		})

		/* row 3 */

		/* start game */
		addItem(object : GuiItem(coords(4, 2)) {
			override fun onClick(player: Player, shift: Boolean) {
				player.closeInventory()
				UHC.startGame { error, message ->
					if (error)
						Commands.errorMessage(player, message)
					else
						Action.sendGameMessage(player, message)
				}
			}

			override fun getStack() =
				ItemCreator.fromType(Material.ENCHANTING_TABLE).name(Component.text("Start Game", NamedTextColor.GOLD))
					.create()
		})
		/* open quirks gui */
		addItem(object : GuiItem(coords(5, 2)) {
			override fun onClick(player: Player, shift: Boolean) = quirkGui.open(player)
			override fun getStack() =
				ItemCreator.fromType(Material.TOTEM_OF_UNDYING)
					.name(Component.text("Quirks", NamedTextColor.LIGHT_PURPLE)).create()
		})
		/* open worldgen gui */
		addItem(object : GuiItem(coords(6, 2)) {
			override fun onClick(player: Player, shift: Boolean) = worldGenGui.open(player)
			override fun getStack() =
				ItemCreator.fromType(Material.GOLD_ORE).name(Component.text("World Gen Options", NamedTextColor.GREEN))
					.create()
		})
		/* reset button */
		addItem(object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = gameConfig.reset()
			override fun getStack() =
				ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name(Component.text("Reset", NamedTextColor.RED))
					.create()
		})
		addItem(CloseButton(coords(8, 2)))
	}
}

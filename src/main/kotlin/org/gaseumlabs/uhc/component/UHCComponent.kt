package org.gaseumlabs.uhc.component

import org.gaseumlabs.uhc.component.UHCStyle.*
import org.gaseumlabs.uhc.util.Util
import net.minecraft.network.chat.*
import net.minecraft.network.chat.ClickEvent.Action.OPEN_URL

class UHCComponent(private var internalComponent: MutableComponent) {
	var locked = false

	fun complete(): MutableComponent {
		return internalComponent
	}

	fun and(string: String, color: TextColor): UHCComponent {
		internalComponent = internalComponent.append(
			TextComponent(string).setStyle(Style.EMPTY.withColor(color))
		)

		return this
	}

	fun and(string: String, color0: TextColor, color1: TextColor): UHCComponent {
		internalComponent = internalComponent.append(Util.nmsGradientString(string, color0.value, color1.value))

		return this
	}

	fun and(string: String, color: TextColor, style: UHCStyle): UHCComponent {
		val component = TextComponent(string).setStyle(Style.EMPTY.withColor(color))

		when (style) {
			BOLD -> component.style = component.style.withBold(true)
			ITALIC -> component.style = component.style.withItalic(true)
		}

		internalComponent = internalComponent.append(component)

		return this
	}

	fun and(string: String, color0: TextColor, color1: TextColor, style: UHCStyle): UHCComponent {
		val component = Util.nmsGradientString(string, color0.value, color1.value)

		when (style) {
			BOLD -> component.style = component.style.withBold(true)
			ITALIC -> component.style = component.style.withItalic(true)
		}

		internalComponent = internalComponent.append(component)

		return this
	}

	fun and(other: UHCComponent): UHCComponent {
		internalComponent = internalComponent.append(other.internalComponent)

		return this
	}

	/* ands */

	fun andIf(cond: Boolean, string: String, color: TextColor): UHCComponent {
		return if (cond) and(string, color) else this
	}

	fun andIf(cond: Boolean, string: String, color: TextColor, style: UHCStyle): UHCComponent {
		return if (cond) and(string, color, style) else this
	}

	fun andIf(cond: Boolean, string: String, color0: TextColor, color1: TextColor): UHCComponent {
		return if (cond) and(string, color0, color1) else this
	}

	fun andIf(cond: Boolean, string: String, color0: TextColor, color1: TextColor, style: UHCStyle): UHCComponent {
		return if (cond) and(string, color0, color1, style) else this
	}

	fun andIf(cond: Boolean, other: UHCComponent): UHCComponent {
		return if (cond) and(other) else this
	}

	/* switches */

	fun andSwitch(cond: Boolean, other: () -> UHCComponent): UHCComponent {
		return if (cond && !locked) {
			locked = true
			and(other())
		} else this
	}

	companion object {
		fun text(): UHCComponent {
			return UHCComponent(TextComponent(""))
		}

		fun text(string: String, color: TextColor): UHCComponent {
			return UHCComponent(
				TextComponent(string).setStyle(Style.EMPTY.withColor(color))
			)
		}

		fun text(string: String, color0: TextColor, color1: TextColor): UHCComponent {
			return UHCComponent(Util.nmsGradientString(string, color0.value, color1.value))
		}

		fun text(string: String, color: TextColor, style: UHCStyle): UHCComponent {
			val component = TextComponent(string).setStyle(Style.EMPTY.withColor(color))

			when (style) {
				BOLD -> component.style = component.style.withBold(true)
				ITALIC -> component.style = component.style.withItalic(true)
			}

			return UHCComponent(component)
		}

		fun text(string: String, color0: TextColor, color1: TextColor, style: UHCStyle): UHCComponent {
			val component = Util.nmsGradientString(string, color0.value, color1.value)

			when (style) {
				BOLD -> component.style = component.style.withBold(true)
				ITALIC -> component.style = component.style.withItalic(true)
			}

			return UHCComponent(component)
		}

		fun link(string: String, link: String, color: TextColor): UHCComponent {
			val component = TextComponent(string)

			component.style = component.style.withClickEvent(ClickEvent(OPEN_URL, link)).withColor(color)

			return UHCComponent(component)
		}
	}
}
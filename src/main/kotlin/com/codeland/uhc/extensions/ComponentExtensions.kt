package com.codeland.uhc.extensions

import com.codeland.uhc.gui.ItemCreator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

object ComponentExtensions {
    fun String.color(hex: Int): Component = Component.text(this).color(TextColor.color(hex))
    fun String.color(color: TextColor): Component = Component.text(this).color(color)

    val String.bold: Component
        get() = Component.text(this).decorate(TextDecoration.BOLD)

    val String.underlined: Component
        get() = Component.text(this).decorate(TextDecoration.UNDERLINED)

    val String.italic: Component
        get() = Component.text(this).decorate(TextDecoration.ITALIC)

    val Component.bold: Component
        get() = this.decorate(TextDecoration.BOLD)

    val Component.underlined: Component
        get() = this.decorate(TextDecoration.UNDERLINED)

    val Component.italic: Component
        get() = this.decorate(TextDecoration.ITALIC)

    val Component.notItalic: Component
        get() = this.decoration(TextDecoration.ITALIC, false)

    operator fun Component.plus(other: Component): Component = this.append(other)
    operator fun Component.plus(other: String): Component = this.append(Component.text(other))
}
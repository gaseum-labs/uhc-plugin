package com.codeland.uhc.util.extensions

import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.CompletableFuture

object RestActionExtensions {
	fun <T> RestAction<T>.submitAsync(): CompletableFuture<T> {
		val future = CompletableFuture<T>()
		this.queue(future::complete, future::completeExceptionally)
		return future
	}

	fun allOf(list: List<CompletableFuture<*>>): CompletableFuture<Void> {
		return CompletableFuture.allOf(*list.toTypedArray())
	}
}

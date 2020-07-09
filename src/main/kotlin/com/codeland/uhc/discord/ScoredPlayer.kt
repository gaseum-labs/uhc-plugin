package com.codeland.uhc.discord

class ScoredPlayer {
	var username: String?
	var score: Float
	var gameCount: Int

	constructor() {
		username = null
		score = 1f
		gameCount = 0
	}

	constructor(uname: String?) {
		username = uname
		score = 0.5f
		gameCount = 0
	}

	constructor(score: Float) {
		username = null
		this.score = score
		gameCount = 0
	}

	override fun toString(): String {
		return "$username $score $gameCount"
	}
}

package com.codeland.uhc.discord.sql

import java.sql.ResultSet

abstract class DatabaseFile <Data, Entry> {
	abstract fun query(): String

	abstract fun parseResults(results: ResultSet): Data

	abstract fun defaultData(): Data

	abstract fun pushQuery(entry: Entry): String
}

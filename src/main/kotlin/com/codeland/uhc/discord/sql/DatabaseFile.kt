package com.codeland.uhc.discord.sql

import java.sql.Connection
import java.sql.ResultSet

abstract class DatabaseFile <Data, Entry> {
	abstract fun query(): String

	abstract fun parseResults(results: ResultSet): Data

	abstract fun defaultData(): Data

	abstract fun pushQuery(entry: Entry): String

	fun push(connection: Connection, entry: Entry) {
		val statement = connection.createStatement()

		try {
			statement.executeQuery(pushQuery(entry))
		} catch (ex: Exception) {
			ex.printStackTrace()
		}

		statement.close()
	}

	companion object {
		fun sqlString(obj: Any): String {
			return "'${obj}'"
		}

		fun sqlNullString(obj: Any?): String {
			return if (obj == null) "NULL" else sqlString(obj)
		}

		fun sqlNull(obj: Any?): String {
			return obj?.toString() ?: "NULL"
		}
	}
}

package com.codeland.uhc.database

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

abstract class DatabaseFile <Data, Entry> {
	abstract fun query(): String

	abstract fun parseResults(results: ResultSet): Data

	abstract fun defaultData(): Data

	abstract fun pushProcedure(): String
	abstract fun pushParams(statement: CallableStatement, entry: Entry)

	abstract fun removeQuery(entry: Entry): String?

	fun push(connection: Connection, entry: Entry): Boolean {
		val callable = connection.prepareCall(pushProcedure())

		pushParams(callable, entry)

		return try {
			callable.execute()
			true
		} catch (ex: Exception) {
			ex.printStackTrace()
			false
		} finally {
			callable.close()
		}
	}

	fun remove(connection: Connection, entry: Entry) {
		sqlQuery(connection, removeQuery(entry) ?: return)
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

		private fun sqlQuery(connection: Connection, query: String) {
			val statement = connection.createStatement()

			try {
				statement.executeQuery(query)
			} catch (ex: Exception) {
				ex.printStackTrace()
			}

			statement.close()
		}
	}
}

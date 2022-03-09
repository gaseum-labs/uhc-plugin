package com.codeland.uhc.database

import java.sql.*

abstract class DatabaseFile<Data, Entry> {
	abstract fun query(): String
	abstract fun parseResults(results: ResultSet): Data

	abstract fun defaultData(): Data

	abstract fun pushProcedure(): String
	abstract fun giveParams(statement: CallableStatement, entry: Entry)

	/* some files do not support remove operation */
	open fun removeProcedure(): String? = null

	fun push(connection: Connection, entry: Entry): Boolean {
		return sqlQuery(connection.prepareCall(pushProcedure()), entry)
	}

	fun remove(connection: Connection, entry: Entry): Boolean {
		return sqlQuery(connection.prepareCall(removeProcedure() ?: return false), entry)
	}

	private fun sqlQuery(callable: CallableStatement, entry: Entry): Boolean {
		giveParams(callable, entry)

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
}

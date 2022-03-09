package com.codeland.uhc.util.extensions

import java.sql.*

object ResultSetExtensions {
	fun ResultSet.getLongNull(index: Int): Long? {
		val result: Long = this.getLong(index)
		return if (this.wasNull()) null else result
	}

	fun ResultSet.getIntNull(index: Int): Int? {
		val result: Int = this.getInt(index)
		return if (this.wasNull()) null else result
	}

	fun CallableStatement.setLongNull(index: Int, value: Long?) {
		if (value == null) this.setNull(index, Types.BIGINT) else this.setLong(index, value)
	}

	fun CallableStatement.setIntNull(index: Int, value: Int?) {
		if (value == null) this.setNull(index, Types.INTEGER) else this.setInt(index, value)
	}
}
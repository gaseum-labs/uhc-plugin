package com.codeland.uhc.discord.sql

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import java.sql.Connection
import java.sql.DriverManager

object DatabaseConnection {
	fun connect(configFile: ConfigFile): Result<Connection> {
		return try {
			val connection = DriverManager.getConnection(
				"jdbc:sqlserver://${configFile.databaseUrl};" +
				"database=${configFile.databaseName};" +
				"user=${configFile.databaseUsername};" +
				"password=${configFile.databasePassword};" +
				"encrypt=false;trustServerCertificate=true;" +
				"loginTimeout=10;"
			)

			Good(connection)

		} catch (ex: Exception) {
			Bad(ex.message)
		}
	}
}

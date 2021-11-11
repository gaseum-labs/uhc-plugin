package com.codeland.uhc.discord

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.discord.database.DatabaseFile
import com.codeland.uhc.discord.database.file.LinkDataFile
import com.codeland.uhc.discord.database.file.LoadoutsFile
import com.codeland.uhc.discord.database.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import com.codeland.uhc.util.*
import com.codeland.uhc.util.Util.void
import java.sql.Connection
import java.sql.DriverManager

class DataManager(
	var connection: Connection?,
	var linkData: LinkDataFile.LinkData,
	var loadouts: Loadouts,
	var nicknames: NicknamesFile.Nicknames
) {
	fun isOnline(): Boolean {
		return connection != null
	}

	companion object {
		val linkDataFile = LinkDataFile()
		val loadoutsFile = LoadoutsFile()
		val nicknamesFile = NicknamesFile()

		private fun connect(configFile: ConfigFile): Result<Connection> {
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

		fun createDataManager(configFile: ConfigFile): MixedResult<DataManager> {
			val result = PreMixedResult()

			val connection = when (val r = connect(configFile)) {
				is Good -> r.value
				is Bad -> result.error(r.error).void()
			} ?: return result.complete(offlineDataManager())

			val statement = connection.createStatement()

			fun <T> load(file: DatabaseFile<T, *>): T {
				return try {
					file.parseResults(statement.executeQuery(file.query()))
				} catch (ex: Exception) {
					result.error(ex.message)
					file.defaultData()
				}
			}

			statement.close()

			val linkData = load(linkDataFile)
			val loadouts = load(loadoutsFile)
			val nicknames = load(nicknamesFile)

			return result.complete(DataManager(connection, linkData, loadouts, nicknames))
		}

		fun offlineDataManager(): DataManager {
			return DataManager(null, linkDataFile.defaultData(), loadoutsFile.defaultData(), nicknamesFile.defaultData())
		}
	}
}

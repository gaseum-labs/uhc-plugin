package com.codeland.uhc.database

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.database.file.LinkDataFile
import com.codeland.uhc.database.file.LoadoutsFile
import com.codeland.uhc.database.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import com.microsoft.sqlserver.jdbc.SQLServerDriver
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.CompletableFuture

class DataManager(
	var connection: Connection?,
	var linkData: LinkDataFile.LinkData,
	var loadouts: Loadouts,
	var nicknames: NicknamesFile.Nicknames
) {
	fun isOnline(): Boolean {
		return connection != null
	}

	/* treat everyone as linked in offline mode */
	fun isLinked(uuid: UUID): Boolean {
		return if (isOnline()) linkData.isLinked(uuid) else true
	}

	fun <T> push(file: DatabaseFile<*, T>, entry: T): CompletableFuture<Boolean> {
		return CompletableFuture.supplyAsync { file.push(connection ?: return@supplyAsync false, entry) }
	}

	fun <T> remove(file: DatabaseFile<*, T>, entry: T): CompletableFuture<Boolean> {
		return CompletableFuture.supplyAsync { file.remove(connection ?: return@supplyAsync false, entry) }
	}

	companion object {
		val linkDataFile = LinkDataFile()
		val loadoutsFile = LoadoutsFile()
		val nicknamesFile = NicknamesFile()

		fun createDataManager(configFile: ConfigFile): CompletableFuture<DataManager> {
			val url = configFile.databaseUrl ?: return CompletableFuture.failedFuture(Exception("No URL in config file"))
			val databaseName = configFile.databaseName ?: return CompletableFuture.failedFuture(Exception("No database name in config file"))
			val username = configFile.databaseUsername ?: return CompletableFuture.failedFuture(Exception("No username in config file"))
			val password = configFile.databasePassword ?: return CompletableFuture.failedFuture(Exception("No password in config file"))

			return CompletableFuture.supplyAsync {
				DriverManager.registerDriver(SQLServerDriver())
				val connection = DriverManager.getConnection(
					"jdbc:sqlserver://${url};DatabaseName=${databaseName};", username, password
				)

				fun <T> load(file: DatabaseFile<T, *>): T {
					val statement = connection.createStatement()

					return try {
						val results = statement.executeQuery(file.query())
						file.parseResults(results)
					} catch (ex: Exception) {
						ex.printStackTrace()
						file.defaultData()
					} finally {
						statement.close()
					}
				}

				DataManager(
					connection,
					load(linkDataFile),
					load(loadoutsFile),
					load(nicknamesFile)
				)
			}
		}

		fun offlineDataManager(): DataManager {
			return DataManager(null, linkDataFile.defaultData(), loadoutsFile.defaultData(), nicknamesFile.defaultData())
		}
	}
}

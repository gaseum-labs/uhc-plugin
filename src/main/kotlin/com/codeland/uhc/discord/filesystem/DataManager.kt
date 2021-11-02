package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.sql.DatabaseFile
import com.codeland.uhc.discord.sql.file.IdsFile
import com.codeland.uhc.discord.sql.file.LinkDataFile
import com.codeland.uhc.discord.sql.file.LoadoutsFile
import com.codeland.uhc.discord.sql.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import com.codeland.uhc.util.MixedResult
import com.codeland.uhc.util.PreMixedResult
import java.sql.Connection

class DataManager(
	var ids: IdsFile.Ids,
	var linkData: LinkDataFile.LinkData,
	var loadouts: Loadouts,
	var nicknames: NicknamesFile.Nicknames
) {
	companion object {
		val idsFile = IdsFile()
		val linkDataFile = LinkDataFile()
		val loadoutsFile = LoadoutsFile()
		val nicknamesFile = NicknamesFile()

		fun createDataManager(connection: Connection): MixedResult<DataManager> {
			val result = PreMixedResult()
			val statement = connection.createStatement()

			fun <T> load(file: DatabaseFile<T, *>): T {
				return try {
					file.parseResults(statement.executeQuery(idsFile.query()))
				} catch (ex: Exception) {
					result.error(ex.message)
					file.defaultData()
				}
			}

			val ids = load(idsFile)
			val linkData = load(linkDataFile)
			val loadouts = load(loadoutsFile)
			val nicknames = load(nicknamesFile)

			return MixedResult(DataManager(ids, linkData, loadouts, nicknames), result)
		}

		fun offlineDataManager(): DataManager {
			return DataManager(idsFile.defaultData(), linkDataFile.defaultData(), loadoutsFile.defaultData(), nicknamesFile.defaultData())
		}
	}
}

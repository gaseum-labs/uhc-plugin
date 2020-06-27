package com.codeland.uhc.command

import java.util.*

class TeamMaker {
    fun getTeamsRandom(names: ArrayList<String>, teamSize: Int): Array<Array<String?>> {
        val ret = ArrayList<Array<String?>>()
        while (names.size > 0) {
            val tem = arrayOfNulls<String>(teamSize)
            for (i in tem.indices) {
                if (names.size > 0) {
                    val rand = (Math.random() * names.size).toInt()
                    tem[i] = names.removeAt(rand)
                }
            }
            ret.add(tem)
        }
        return ret.toArray(arrayOf())
    }
}
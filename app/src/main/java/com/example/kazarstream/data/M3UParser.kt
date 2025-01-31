package com.example.kazarstream.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object M3UParser {
    fun parse(url: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup: String? = null

        try {
            val connection = URL(url).openConnection()
            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    when {
                        line.startsWith("#EXTINF:") -> {
                            val logoMatch = "tvg-logo=\"([^\"]+)\"".toRegex().find(line)
                            val groupMatch = "group-title=\"([^\"]+)\"".toRegex().find(line)
                            val nameMatch = ",[\\s]*(.+)$".toRegex().find(line)

                            currentLogo = logoMatch?.groupValues?.get(1)
                            currentGroup = groupMatch?.groupValues?.get(1)
                            currentName = nameMatch?.groupValues?.get(1) ?: ""
                        }
                        !line.startsWith("#") && line.isNotBlank() -> {
                            if (currentName.isNotEmpty()) {
                                channels.add(Channel(
                                    name = currentName,
                                    logoUrl = currentLogo,
                                    streamUrl = line.trim(),
                                    groupTitle = currentGroup
                                ))
                            }
                        }
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return channels
    }
} 
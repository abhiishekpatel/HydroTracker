package com.example.hydrotracker.data.repository

import com.example.hydrotracker.data.local.WaterEntry
import com.example.hydrotracker.data.local.WaterEntryDao
import com.example.hydrotracker.data.remote.RemoteHydrationLog
import com.example.hydrotracker.data.remote.supabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class HydrationSyncRepository(private val dao: WaterEntryDao) {

    suspend fun syncToCloud(userId: String) {
        val unsynced = dao.getUnsyncedEntries()
        if (unsynced.isEmpty()) return

        unsynced.forEach { entry ->
            val syncId = UUID.randomUUID().toString()
            val log = RemoteHydrationLog(
                id = syncId,
                userId = userId,
                amountMl = entry.amountMl,
                timestamp = entry.timestamp,
                date = entry.date
            )
            runCatching {
                supabaseClient.postgrest["hydration_logs"].insert(log)
                dao.markSynced(entry.id, syncId)
            }
        }
    }

    suspend fun mergeFromCloud() {
        val existingSyncIds = dao.getAllSyncIds().toSet()
        val remoteLogs = runCatching {
            supabaseClient.postgrest["hydration_logs"]
                .select()
                .decodeList<RemoteHydrationLog>()
        }.getOrNull() ?: return

        val newEntries = remoteLogs
            .filter { it.id !in existingSyncIds }
            .map { log ->
                WaterEntry(
                    amountMl = log.amountMl,
                    timestamp = log.timestamp,
                    date = log.date,
                    syncId = log.id,
                    isSynced = true
                )
            }

        if (newEntries.isNotEmpty()) {
            dao.insertAll(newEntries)
        }
    }
}

package com.example.hydrotracker.data.repository

import com.example.hydrotracker.data.remote.RemoteProfile
import com.example.hydrotracker.data.remote.supabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ProfileRepository {

    suspend fun fetchProfile(): RemoteProfile? {
        return runCatching {
            supabaseClient.postgrest["profiles"]
                .select(Columns.ALL)
                .decodeSingle<RemoteProfile>()
        }.getOrNull()
    }

    suspend fun upsertProfile(profile: RemoteProfile) {
        runCatching {
            supabaseClient.postgrest["profiles"].upsert(profile)
        }
    }
}

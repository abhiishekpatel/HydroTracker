package com.example.hydrotracker.data.repository

import com.example.hydrotracker.data.remote.supabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {

    fun getSessionStatus() = supabaseClient.auth.sessionStatus

    suspend fun login(email: String, password: String): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun logout() {
        runCatching { supabaseClient.auth.signOut() }
    }

    fun currentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }
}

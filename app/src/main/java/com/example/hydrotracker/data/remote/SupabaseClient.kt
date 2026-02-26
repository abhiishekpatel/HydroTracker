package com.example.hydrotracker.data.remote

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout

private const val SUPABASE_URL = "https://sjnhzewxadwvunzxqmxj.supabase.co"
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNqbmh6ZXd4YWR3dnVuenhxbXhqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzIwMDE0MjQsImV4cCI6MjA4NzU3NzQyNH0.7_csMwZitqiP95w5ASMzvR_si-iOOu5HC5s38A_moCs"

private var _supabaseClient: SupabaseClient? = null

val supabaseClient: SupabaseClient
    get() = _supabaseClient ?: error("Call initSupabaseClient(context) before using the client")

@OptIn(SupabaseInternal::class)
fun initSupabaseClient(context: Context) {
    if (_supabaseClient != null) return
    _supabaseClient = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
        install(Auth) {
            sessionManager = SharedPrefsSessionManager(context.applicationContext)
        }
        install(Postgrest)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 30_000L
            }
            // Retry on connection failure (handles flaky networks and cold Supabase project wake-up)
            engine {
                (this as? io.ktor.client.engine.okhttp.OkHttpConfig)?.config {
                    retryOnConnectionFailure(true)
                }
            }
        }
    }
}

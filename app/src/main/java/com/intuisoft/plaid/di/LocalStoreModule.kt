package com.intuisoft.plaid.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.intuisoft.plaid.local.UserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val preferencesModule = module {
    single(named("userPreferences")) { provideUserPreferences(androidContext()) }
    factory { UserPreferences(get(named("userPreferences")), get()) }
}

fun provideUserPreferences(context: Context): SharedPreferences {
    return provideEncryptedPreference(context, UserPreferences.SHARED_PREFS_NAME)
}

fun provideEncryptedPreference(context: Context, name: String): SharedPreferences {
    val spec = KeyGenParameterSpec.Builder(
        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
        .build()

    val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(spec)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        name,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

package com.st.demo.model

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SecureStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "MyEncryptedPrefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveJwt(jwt: String) {
        val editor = sharedPreferences.edit()
        editor.putString("jwt", jwt)
        editor.apply()
    }

    fun getJwt(): String? {
        return sharedPreferences.getString("jwt", null)
    }



    fun saveRole(role: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("role", role)
        editor.apply()
    }

    fun clear(key:String){
        sharedPreferences.edit {
            remove(key)
        }
    }

    fun saveUser(user: User) {
        sharedPreferences.edit {
            putString("user_id", user.id)
            putString("user_name", user.nome)
            putString("user_email", user.email)
            putString("user_cognome", user.cognome)
            putString("role", user.role)
        }
    }

    fun getUser(): User {
        val user = User()
        user.id = sharedPreferences.getString("user_id", null)
        user.nome = sharedPreferences.getString("user_name", null)
        user.email = sharedPreferences.getString("user_email", null)
        user.cognome = sharedPreferences.getString("user_cognome", null)
        user.role = sharedPreferences.getString("role",null)
        return user
    }

    fun isUserSaved(): Boolean {
        return sharedPreferences.getString("user_id", null) != null
    }

    fun clearUser() {
        sharedPreferences.edit {
            remove("user_id")
            remove("user_name")
            remove("user_email")
            remove("user_cognome")
            remove("user_data_nascita")
        }
    }

    fun saveDeviceId(deviceId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("device_id", deviceId)
        editor.apply()
    }

    fun getDeviceId(): String? {
        return sharedPreferences.getString("device_id", null)
    }

    fun clearDeviceId() {
        val editor = sharedPreferences.edit()
        editor.remove("device_id")
        editor.apply()
    }
}

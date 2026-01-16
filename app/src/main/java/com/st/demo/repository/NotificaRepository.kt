package com.unisalento.wotproject20242025potholedetect.repository

import com.st.demo.model.Notifica
import com.st.demo.wrappers.Resource


interface NotificaRepository {

    suspend fun getNotify( token: String): Resource<Notifica>

    suspend fun deleteNotify(token: String,notifica: Notifica): Resource<Notifica>

    suspend fun setAdminIsLetta(token: String,id: String): Resource<Notifica>

    suspend fun setUserIsLetta(token: String,id: String): Resource<Notifica>
}
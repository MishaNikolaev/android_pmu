package com.nmichail.android_pmu.domain.repository

import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User

interface UserRepository {

    suspend fun getAll(): List<User>

    suspend fun getById(id: Long): User?

    suspend fun register(player: Player): User
}
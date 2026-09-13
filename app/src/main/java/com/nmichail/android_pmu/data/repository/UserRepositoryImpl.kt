package com.nmichail.android_pmu.data.repository

import com.nmichail.android_pmu.data.dao.UserDao
import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User
import com.nmichail.android_pmu.domain.repository.UserRepository

// TODO: Андрей, твоя часть
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getAll(): List<User> {
        TODO("Андрей, твоя часть")
    }

    override suspend fun getById(id: Long): User? {
        TODO("Андрей, твоя часть")
    }

    override suspend fun register(player: Player): User {
        TODO("Андрей, твоя часть")
    }
}
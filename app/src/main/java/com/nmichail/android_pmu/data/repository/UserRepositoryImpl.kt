package com.nmichail.android_pmu.data.repository

import com.nmichail.android_pmu.data.dao.UserDao
import com.nmichail.android_pmu.data.mapper.toDomain
import com.nmichail.android_pmu.data.mapper.toEntity
import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User
import com.nmichail.android_pmu.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getAll(): List<User> {
        return userDao.getAll().map { it.toDomain() }
    }

    override suspend fun getById(id: Long): User? {
        return userDao.getById(id)?.toDomain()
    }

    override suspend fun register(player: Player): User {
        val userId = userDao.insert(player.toEntity(id = 0))
        return userDao.getById(userId)?.toDomain()
            ?: error("User was not saved")
    }
}
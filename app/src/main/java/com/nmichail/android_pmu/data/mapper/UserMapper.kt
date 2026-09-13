package com.nmichail.android_pmu.data.mapper

import com.nmichail.android_pmu.data.entity.UserEntity
import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        surname = surname,
        otchestvo = otchestvo,
        course = course,
        difficulty = difficulty,
        birthDay = birthDay,
        birthMonth = birthMonth,
        birthYear = birthYear,
        gender = gender,
        zodiac = zodiac
    )
}

fun Player.toEntity(id: Long): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        surname = surname,
        otchestvo = otchestvo,
        course = course,
        difficulty = difficulty,
        birthDay = birthDay,
        birthMonth = birthMonth,
        birthYear = birthYear,
        gender = gender,
        zodiac = zodiac
    )
}

fun User.toPlayer(): Player {
    return Player(
        name = name,
        surname = surname,
        otchestvo = otchestvo,
        course = course,
        difficulty = difficulty,
        birthDay = birthDay,
        birthMonth = birthMonth,
        birthYear = birthYear,
        gender = gender,
        zodiac = zodiac
    )
}
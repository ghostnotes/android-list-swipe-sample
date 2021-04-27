package co.ghostnotes.sample.list.swipe.util

import co.ghostnotes.sample.list.swipe.model.User
import kotlin.random.Random

class DummyDataUtil {

    fun createDummyUsers(size: Int = 100): List<User> {
        val randomAges = List(size) { Random.nextInt(0, 100) }
        return (1..size).map { i ->  User(i.toLong(), "User name $i", randomAges[i - 1]) }
    }
}
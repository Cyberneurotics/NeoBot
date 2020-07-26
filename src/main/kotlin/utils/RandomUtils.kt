package utils

import java.util.*

object RandomUtils {
    private val random = Random()

    /**
     * 获得一个[0,max)之间的整数。
     */
    fun getRandomInt(max: Int): Int {
        return getRandomInt(0, max)
    }

    /**
     * 获得一个[min,max)之间的整数。
     */
    fun getRandomInt(min: Int, max: Int): Int {
        return Math.abs(random.nextInt()) % max + min
    }
}
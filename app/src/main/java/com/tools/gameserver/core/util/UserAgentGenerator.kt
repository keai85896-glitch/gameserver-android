package com.tools.gameserver.core.util

import java.util.Random

/**
 * User-Agent 随机生成器
 */
object UserAgentGenerator {

    private val random = Random()

    private val FIXED_UAS = listOf(
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.163 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 14; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.6167.101 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
    )

    private val CHROME_VERSIONS = listOf("118", "119", "120", "121", "122", "123")
    private val ANDROID_VERSIONS = listOf("12", "13", "14")
    private val DEVICE_MODELS = listOf("SM-S928B", "SM-G998B", "Pixel 8", "Pixel 7 Pro", "SM-A546B", "SM-S901B")

    fun randomUserAgent(): String {
        return if (random.nextBoolean()) {
            FIXED_UAS[random.nextInt(FIXED_UAS.size)]
        } else {
            val chrome = CHROME_VERSIONS[random.nextInt(CHROME_VERSIONS.size)]
            val android = ANDROID_VERSIONS[random.nextInt(ANDROID_VERSIONS.size)]
            val device = DEVICE_MODELS[random.nextInt(DEVICE_MODELS.size)]
            val build = "${random.nextInt(50000, 60000)}.0.${random.nextInt(100, 999)}"
            "Mozilla/5.0 (Linux; Android $android; $device) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chrome.0.$build Mobile Safari/537.36"
        }
    }
}
package modules

import com.alibaba.fastjson.JSON
import config.SourceConfig
import utils.HttpUtils.get
import utils.RandomUtils
import java.io.IOException
import java.net.URL

class YandereModule {
    enum class Rating {
        UNKNOWN, SAFE, QUESTIONABLE, EXPLICIT
    }

    fun rating(rating: String): Rating {
        var rating = rating
        rating = rating.trim { it <= ' ' }
        return when (rating) {
            "s", "S", "safe" -> Rating.SAFE
            "q", "Q", "questionable" -> Rating.QUESTIONABLE
            "e", "E", "explicit" -> Rating.EXPLICIT
            else -> Rating.UNKNOWN
        }
    }

    fun randomPic(rating: Rating, orig: Boolean): URL? {
        return randomPic(rating, null, orig)
    }

    fun randomPic(tags: String?): URL? {
        return randomPic(Rating.SAFE, tags, false)
    }

    @JvmOverloads
    fun randomPic(
        rating: Rating = Rating.SAFE,
        tags: String? = null,
        orig: Boolean = false
    ): URL? {
        return randomPic(rating, tags, 100, orig)
    }

    private fun randomPic(
        rating: Rating,
        tags: String?,
        bound: Int,
        orig: Boolean
    ): URL? {
        var rating = rating
        var tags = tags
        if (bound == 0) {
            return null
        }
        if (rating == Rating.UNKNOWN) {
            rating = Rating.SAFE
        }
        if (tags == null) {
            tags = ""
        }
        val page = RandomUtils.getRandomInt(bound) + 1
        val url =
            SourceConfig.SOURCE_YANDERE + "?limit=100&page=" + page + "&tags=" + tags + "%20rating:" + rating.name.toLowerCase()
        val response = get(url) ?: return null
        //String response = null;
//        try {
//            response = Jsoup.connect(url).proxy("127.0.0.1", 1080).ignoreContentType(true).get().html();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        val respArray = JSON.parseArray(response)
        if (respArray == null || respArray.isEmpty()) {
            return randomPic(rating, tags, bound / 2, orig)
        }
        val rc = RandomUtils.getRandomInt(respArray.size)
        val respJson = respArray.getJSONObject(rc)
        return try {
            val key = if (orig) "file_url" else "sample_url"
            URL(respJson.getString(key))
            //return new CQImage(respJson.getString(key));
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
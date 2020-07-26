package modules

import com.alibaba.fastjson.JSON
import utils.HttpUtils.get
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class SaucenaoModule {
    fun bestMatch(imgUrl: String?): Map<String, String> {
        val result: MutableMap<String, String> =
            HashMap()
        if (imgUrl == null || imgUrl.isEmpty()) {
            return result
        }
        var url = baseUrl
        url += try {
            "?db=999&output_type=2&testmode=1&url=" + URLEncoder.encode(imgUrl, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return result
        }
        val response = get(url)
        val respJson = JSON.parseObject(response).getJSONArray("results")
        if (respJson.isEmpty()) {
            return result
        }
        val dataJson = respJson.getJSONObject(0).getJSONObject("data")
        dataJson.forEach { k: String, v: Any? ->
            if (v != null) {
                val value: String
                value = if (v is JSON) {
                    v.toJSONString()
                } else {
                    v.toString()
                }
                result[k] = value
            }
        }
        return result
    }

    companion object {
        private const val baseUrl = "https://saucenao.com/search.php"
    }
}
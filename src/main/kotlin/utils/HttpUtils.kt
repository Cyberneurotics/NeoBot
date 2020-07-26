package utils

import com.alibaba.fastjson.JSONObject
import map
import org.apache.commons.io.FileUtils
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

object HttpUtils {
    ///private static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private val proxy = map?.get("proxy") as JSONObject
    private val httpClient = if(proxy["on"] as Boolean)  HttpClients.custom().setDefaultRequestConfig(
        RequestConfig.custom().setProxy(
            HttpHost(proxy["host"] as String, proxy["port"] as Int, "http")
        )
            .build()
    ).build() else HttpClients.custom().build()

    operator fun get(url: String?): String? {
        var httpResponse: CloseableHttpResponse? = null
        return try {
            httpResponse = httpClient.execute(HttpGet(url))
            EntityUtils.toString(httpResponse.entity)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun httpGet(str: String?): String? {
        try {
            val url = URL(str)
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 1080))
            val conn = url.openConnection(proxy) as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()
            val responseCode = conn.responseCode
            println("GET Response Code :: $responseCode")
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                val `in` = BufferedReader(
                    InputStreamReader(
                        conn.inputStream
                    )
                )
                var inputLine: String?
                val response = StringBuffer()
                while (`in`.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                `in`.close()

                // print result
                println(response.toString())
                return response.toString()
            } else {
                println("GET request not worked")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun httpGetImg(imgUrl: String, savePath: String?) {
        // 发送get请求
        val request = HttpGet(imgUrl)
        // lateinit var requestConfig: RequestConfig
        val requestConfig = if(((map?.get("proxy") as JSONObject)["on"] as Boolean)){
            val host = HttpHost(proxy["host"] as String, proxy["port"] as Int)
            RequestConfig.custom()
                .setSocketTimeout(60000).setConnectTimeout(60000).setProxy(host).build()
        }else{
            RequestConfig.custom()
                .setSocketTimeout(60000).setConnectTimeout(60000).build()
        }

        //设置请求头
        request.setHeader(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1"
        )
        request.config = requestConfig
        try {
            val response = httpClient.execute(request)
            if (HttpStatus.SC_OK == response.statusLine.statusCode) {
                val entity = response.entity
                val `in` = entity.content
                FileUtils.copyInputStreamToFile(`in`, File(savePath))
                println("下载图片成功:$imgUrl")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException(e)
        } finally {
            request.releaseConnection()
        }
    }
}
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import modules.SaucenaoModule
import modules.YandereModule
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.queryUrl
import net.mamoe.mirai.message.recall
import utils.HttpUtils
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

private val INVALID = arrayOf("u")
private val groupPattern =
    Pattern.compile("\\s*[Hh]\\s*([sSqQ])?\\s*(\\d+)?\\s*(aqua)?\\s*(o)?(\\s*([a-zA-Z_!\\?\\(\\)]+))?")
private val friendPattern =
    Pattern.compile("\\s*[Hh]\\s*([sSqQeE])?\\s*(\\d+)?\\s*(aqua)?\\s*(o)?(\\s*([a-zA-Z_!\\?\\(\\)]+))?")

fun splitMsg(str: String?, pattern: Pattern): Array<String>? {
    if (str == null) {
        return INVALID
    }
    val m = pattern.matcher(str)
    return if (m.matches()) {
        val s1 = if (m.group(1) == null) "" else m.group(1)
        val s2 = if (m.group(2) == null) "" else m.group(2)
        val s3 = if (m.group(3) == null) "" else m.group(3)
        val s4 = if (m.group(4) == null) "" else m.group(4)
        val s5 = if (m.group(5) == null) "" else m.group(5).replace("\\s".toRegex(), "")
        arrayOf(s1, s2, s3, s4, s5)
    } else {
        INVALID
    }
}

suspend fun getPicSrc(e: MessageEvent): Boolean {
    if(((map?.get("getSrc") as JSONObject)["on"] as Boolean).not()){
        return false;
    }
    if (e.message.content.startsWith("src")) {
        val img = e.message[Image] ?: return false
        val imgUrl = img.queryUrl()
        val saucenaoModule = SaucenaoModule()
        val bestMatch: Map<String, String> = saucenaoModule.bestMatch(imgUrl)
        if (bestMatch.isNotEmpty()) {
            val sb = StringBuilder()
            bestMatch.forEach { (k: String?, v: String?) ->
                sb.append(
                    k
                ).append(" --> ").append(v).append("\n")
            }
            e.reply(sb.toString())
        } else {
            e.reply("无匹配源")
        }
        return true
    }
    return false
}

suspend fun sendPic(e: MessageEvent): Boolean {
    if(((map?.get("sendPic") as JSONObject)["on"] as Boolean).not()){
        return false;
    }
    val msg = e.message.content
    val yandereModule = YandereModule()
    val rating: YandereModule.Rating
    val picNums: Int
    val aqua: Boolean
    val orig: Boolean
    val tag: String
    val msgs = splitMsg(msg, if(e is GroupMessageEvent) groupPattern else friendPattern)
    if (msgs != null && msgs.contentEquals(INVALID)) {
        return false
    }
    rating = if (msgs!![0].isEmpty()) yandereModule.rating("s") else yandereModule.rating(msgs[0])
    picNums = if (msgs[1].isEmpty()) 1 else msgs[1].toInt().coerceAtMost(10)
    aqua = msgs[2].isNotEmpty()
    orig = msgs[3].isNotEmpty()
    tag = msgs[4]

    var imgUrl : URL?
    val hashSet = HashSet<String>()
    var sent = false
    val recall = (map?.get("sendPic") as JSONObject)["recall"] as JSONObject
    val receipts = ArrayList<MessageReceipt<Contact>>()
    val queue: Queue<MessageReceipt<Contact>> = LinkedList()

    for (i in 0 until picNums) {
        imgUrl = when {
            aqua -> {
                yandereModule.randomPic(rating, "minato_aqua", orig)
            }
            tag.isNotEmpty() -> {
                yandereModule.randomPic(rating, tag, orig)
            }
            else -> {
                yandereModule.randomPic(rating, orig)
            }
        }
        if(imgUrl == null){
            continue
        }
        if (!hashSet.contains(imgUrl.toString())) {
            hashSet.add(imgUrl.toString())
            try {
                val fileName = "temp.jpg"
                HttpUtils.httpGetImg(imgUrl.toString(), fileName)
                val r = e.sendImage(File(fileName))
                //receipts.add(r)
                //queue.add(r)
                if((recall["on"] as Boolean) && rating != YandereModule.Rating.SAFE){
                    GlobalScope.launch {
                        delay((recall["millis"] as Number).toLong())
                        r.recall()
                    }
                }

                sent = true
            } catch (e: IOException) {
                e.printStackTrace()
                //recallPics(receipts, rating)
                return sent
            }
        }
    }
    //recallPics(receipts, rating)
    return sent
}

suspend fun recallPics(receipts: ArrayList<MessageReceipt<Contact>>, rating:YandereModule.Rating){
    val recall = (map?.get("sendPic") as JSONObject)["recall"] as JSONObject
    if((recall["on"] as Boolean).not()) return
    if(receipts.isNotEmpty() && rating != YandereModule.Rating.SAFE){
        Thread.sleep((recall["millis"] as Number).toLong())
        for(r in receipts){
            r.recall()
        }
    }
}
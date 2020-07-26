
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import utils.parseJson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

suspend fun groupReply(e:GroupMessageEvent){
    map = parseJson(jsonPath)
    val msg = e.message.content

    if(help(e, msg) || updateQA(msg) || checkQA(e) || bullshit(e)){
        return
    }

    if(getPicSrc(e) || sendPic(e)){
        return
    }


    if(replyAt(e) || repeatThird(e) || replySecond(e)){
        return
    }

}


var atReply = arrayOf(
    "爬", "外币外币", "外币巴伯", "阿巴阿巴", "?", "¿",
    "泥嚎，我很可爱，请亏我全"
)
var repeat = arrayOf("买", "冲", "上号", "🐂")

suspend fun pic(e:MessageEvent):Boolean{
    if(e.message.content.startsWith("pic")){
        //val chain = "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai".parseMiraiCode()
        val img = Image("{657740CB-B1E7-3CAB-676A-AD51A3D96D59}.mirai")
        img.sendTo(e.subject)
        return true
    }
    return false
}

suspend fun replyAt(e:MessageEvent): Boolean {
    if(((map?.get("replyAt") as JSONObject)["on"] as Boolean).not()){
        return false;
    }
    val at : At = e.message[At] ?: return false
    if(at.target == e.bot.selfQQ.id){
        e.reply(At(e.sender as Member) + atReply.random())
        return true
    }
    return false
}

suspend fun replySecond(e: GroupMessageEvent): Boolean{
    if(((map?.get("repeat") as JSONObject)["second"] as Boolean).not()){
        return false;
    }
    val ratio = ((map?.get("repeat") as JSONObject)["secondRatio"]) as Double

    val msg  = e.message.content
    for (str in repeat) {
        if (msg.startsWith(str)) {
            if (Math.random() < ratio) {
                e.reply(str)
                return true
            }
        }
    }
    return false
}

fun sameMessage(m1: MessageChain, m2:MessageChain):Boolean{
    if(m1.size == 1 || m2.size == 1 || m1.size != m2.size) return false
    for(i in 1 until m1.size){
        if(m1[i].toString() != m2[i].toString()){
            return false
        }
    }
    return true
}


var strMap = HashMap<Long, MessageChain>()
suspend fun repeatThird(e: GroupMessageEvent): Boolean {
    if(((map?.get("repeat") as JSONObject)["third"] as Boolean).not()){
        return false
    }
    val repeat = (map?.get("repeat") as JSONObject)
    val ratio = ((map?.get("repeat") as JSONObject)["thirdRatio"]) as Double

    val group = e.group.id
    val content = e.message.content
    return if (strMap.containsKey(group)) {
        val msgChn = strMap[group] ?: return false

        if (sameMessage(e.message, msgChn) && Math.random() < ratio) {
            e.reply(msgChn)
            strMap.remove(group)
            true
        } else {
            strMap.remove(group)
            strMap[group] = e.message
            false
        }
    } else {
        strMap[group] = e.message
        false
    }
}

suspend fun help(e:MessageEvent, msg: String): Boolean {
    if (msg == "help") {
        e.reply(
            """
                私发色图：h[s/q][number][o] [tag]  
                p.s []都为可选项 s=safe, q=questionable, number=数量，最大为10, o=原图, tag=搜索标签,如'final_fantasy'
                Q问题 A回答 即可让机器人以后自动回答
                小作文 主题 字数 写出某一主题某一字数的小作文 如"小作文 害怕 100" 不加数字默认100字
                """.trimIndent()
        )
        return true
    }
    return false
}

var qaPattern = Pattern.compile("[Qq](.+)\\s+[Aa](.+)")
var QA = HashMap<String, String>()

fun updateQA(msg: String?): Boolean {
    if (msg == null || ((map?.get("qa") as JSONObject)["on"] as Boolean).not()) {
        return false
    }
    val m: Matcher = qaPattern.matcher(msg)
    if (m.matches()) {
        val q = m.group(1)
        val a = m.group(2)
        QA.put(q, a)
        return true
    }
    return false
}

suspend fun checkQA(e:MessageEvent): Boolean {
    if(((map?.get("replyAt") as JSONObject)["on"] as Boolean).not()){
        return false
    }

    val msg = e.message.content
    if (QA.containsKey(msg)) {
        QA[msg]?.let { e.reply(it) }
        return true
    }
    return false
}

suspend fun bullshit(e:MessageEvent): Boolean {
    if(((map?.get("bullshit") as JSONObject)["on"] as Boolean).not()){
        return false
    }
    val msg = e.message.content
    if (msg.startsWith("小作文") || msg.startsWith("作文")) {
        val strs = msg.split(" ").toTypedArray()
        if (strs.size <= 1) {
            return false
        }

        //String res = HttpUtils.get("https://suulnnka.github.io/BullshitGenerator/index.html?%E4%B8%BB%E9%A2%98=%E5%AD%A6%E7%94%9F%E4%BC%9A%E9%80%80%E4%BC%9A&%E9%9A%8F%E6%9C%BA%E7%A7%8D%E5%AD%90=2002859472") ;
        val theme = strs[1]
        val words = if (strs.size >= 3 && strs[2].toIntOrNull() != null) strs[2] else "100"
        if (theme == null || theme.isEmpty()) {
            return false
        }
        val command =
            "cmd /c python C:\\Users\\shiro\\IdeaProjects\\hentai-bot\\bs\\bs.py $theme C:\\Users\\shiro\\IdeaProjects\\hentai-bot\\bs\\data.json $words"
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec(command)
            p.waitFor()
            val bri = BufferedReader(InputStreamReader(p.inputStream, "GB2312"))
            var line: String?
            val sb = StringBuilder()
            while (bri.readLine().also { line = it } != null) {
                sb.append(line)
                println(line)
            }
            bri.close()
            p.waitFor()
            println("Done.")

            e.reply(sb.toString())
            p.destroy()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    return false
}
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.join
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.GroupMessageEvent
import utils.parseJson


//val qqId = 3250263844L //二号机
//val qqId = 3531714390L //爱酱
//val password = "shiro990805"

const val jsonPath = "src/main/resources/params.json"
var map = parseJson(jsonPath)

suspend fun main(args: Array<String>) {
    val bot = Bot(map?.get("qqId") as Long, map?.get("password") as String){
       fileBasedDeviceInfo()
       inheritCoroutineContext()
    }.alsoLogin()
    bot.subscribeAlways<GroupMessageEvent> { event -> groupReply(event) }
    bot.subscribeAlways<FriendMessageEvent> { event -> friendReply(event) }

    bot.join()
}
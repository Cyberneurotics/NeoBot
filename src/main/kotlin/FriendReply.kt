import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.data.content

suspend fun friendReply(e:FriendMessageEvent){
    val msg = e.message.content

    if(help(e, msg) || updateQA(msg) || checkQA(e) || bullshit(e)){
        return
    }

    if(getPicSrc(e) || sendPic(e)){
        return
    }
}
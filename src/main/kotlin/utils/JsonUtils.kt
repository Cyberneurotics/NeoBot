package utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.parser.Feature
import java.io.File


fun parseJson(path: String) : JSONObject? {
    val jsonStr:String = File(path).readText()
    val disableDecimalFeature = JSON.DEFAULT_PARSER_FEATURE and Feature.UseBigDecimal.getMask().inv()
    val type: Class<*> = JSONObject::class.java
    return JSON.parseObject(jsonStr, type, disableDecimalFeature)
}
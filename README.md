# NeoBot
基于[Mirai](https://github.com/mamoe/mirai) 的QQ机器人，主要功能为发色图（笑）。

# 功能介绍
所有功能都在json文件中设置开关与参数
## 复读
若群里有两句重复，以一定概率复读。

## 回复At
若群成员@机器人，自动回复

## 色图
匹配如下正则表达式：\\s*[Hh]\\s*([sSqQ])?\\s*(\\d+)?\\s*(aqua)?\\s*(o)?(\\s*([a-zA-Z_!\\?\\(\\)]+))?

h开头，s/q选择尺度（不填默认s），数字（图片数量），o（是否原图），空格后tag（如final_fantasy)

e.g.   h3, hq10o, hq3 minato_aqua

如果怕炸号可以在json文件中选择开启撤回与延迟的时间，每张图片将在发出后指定时间后撤回

参考[hentaibot](https://github.com/lywbh/hentai-bot)

## 寻找图源
src+[图片]

机器人会自动回复图源

## QA
输入 Q问题 A答案 下次输入问题时机器人会自动回复答案

## bullshit
输入 小作文 主题 字数 可以让机器人写一段[生成器](https://github.com/menzi11/BullshitGenerator)生成的狗屁不通的小作文

# 安装
1. 安装JDK8+, git clone
2. 在[example.json](./example.json)中设置QQ号与密码 在[Login.kt](./src/main/kotlin/Login.kt)中设置json地址
3. 运行[Login.kt](./src/main/kotlin/Login.kt)

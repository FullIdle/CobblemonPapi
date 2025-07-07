package org.figsq.cobblemonpapi.cobblemonpapi

import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.registry.BiomeTagCondition
import com.cobblemon.mod.common.util.asTranslated
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.core.RegistryAccess
import org.bukkit.OfflinePlayer
import java.text.DecimalFormat


//[]是需要带的,{}是不需要带的
//{box} -1 == party {box} > -1 == PCBox
//玩家存储内获取精灵数据 %cobblemonpapi_storage:[{box},{slot}]_{arg}_{可选参数}%
//更具编号获取精灵数据 %cobblemonpapi_dex:{dex}_{arg}_{可选参数}%
//玩家存储内获取精灵数据 %cobblemonpapi_species:{species}_{arg}_{可选参数}%

class PapiArgs(player: OfflinePlayer?, pokemon: Pokemon, args: Array<String>) {
    //有nickname结果就是nickname没有则是翻译名
    val displayname: String by lazy { pokemon.getDisplayName().string }

    //玩家自定义宝可梦的名字 没有则为NO-NICKNAME
    val nickname: String by lazy { pokemon.nickname?.string ?: "NO-NICKNAME" }

    //宝可梦物种名
    val pokename: String by lazy { pokemon.species.name }

    //翻译名(看cobblemon用的是哪个语言文件)
    val translatedname: String by lazy {
        pokemon.species.translatedName.string
    }

    //编号
    //可选参数 当可选参数是 000 时编号为1时结果为: 001 超过三位数时则和正常数字显示一样 (还可以是00000 0000要多少位数就写多少个0)
    val dex: String by lazy {
        args.getOrNull(0)?.let {
            DecimalFormat(it).format(pokemon.species.nationalPokedexNumber)
        } ?: pokemon.species.nationalPokedexNumber.toString()
    }

    //获取主人的名字 只判断玩家的NPC不管
    val ownername: String by lazy {
        pokemon.getOwnerUUID()?.let {
            it.getOfflinePlayer()?.run {
                this.name
            }
        } ?: "NO-OWNER"
    }

    //性格
    val nature: String by lazy { pokemon.nature.displayName.asTranslated().string }

    //特性
    val ability: String by lazy { pokemon.ability.displayName.asTranslated().string }

    //特性描述
    val abilitydesc: String by lazy { pokemon.ability.description.asTranslated().string }

    //是否闪光
    val shiny: String by lazy { pokemon.shiny.toString() }

    //等级
    val level: String by lazy { pokemon.level.toString() }

    //现等级的经验
    val exp: String by lazy { pokemon.experience.toString() }

    //性别
    val gender: String by lazy { pokemon.gender.name }

    //所在存储-1就是背包大于-1的是pc的页
    val box: String by lazy {
        pokemon.storeCoordinates.get()?.let {
            when (it.position) {
                is PartyPosition -> "-1"
                is PCPosition -> (it.position as PCPosition).box.toString()
                else -> "NO-BOX"
            }
        } ?: "NO-BOX"
    }

    //所在位置
    val slot: String by lazy {
        pokemon.storeCoordinates.get()?.let {
            when (it.position) {
                is PartyPosition -> (it.position as PartyPosition).slot
                is PCPosition -> (it.position as PCPosition).slot
                else -> "NO-SLOT"
            }.toString()
        } ?: "NO-SLOT"
    }

    //精灵类型(属性,火,格斗,飞行这类)
    //可选参数 索引Int 不加参数则是所有类型一起
    val type: String by lazy {
        args.getOrNull(0)?.let { index ->
            pokemon.types.toList()[index.toInt()].displayName.string
        } ?: pokemon.types.joinToString { it.displayName.string }
    }

    //持有道具
    val held: String by lazy { pokemon.heldItem().displayName.string }

    //技能
    //可选参数1 索引Int 不加参数则是所有技能一起
    //可选参数2 在有参数1的情况下使用参数2来获取技能的数据
    //name(和不写效果一样) type(属性) maxpp(最大pp) currentpp(当前pp) power(威力) accuracy(命中率) damagecategory(伤害类型) desc(描述)
    val moveset: String by lazy {
        (args.getOrNull(0)?.let { index ->
            pokemon.moveSet[index.toInt()]?.let {
                when (args.getOrNull(1)?.lowercase()) {
                    null, "name" -> it.displayName.string
                    "type" -> it.type.displayName.string
                    "maxpp" -> it.maxPp
                    "currentpp" -> it.currentPp
                    "power" -> it.power
                    "accuracy" -> it.accuracy
                    "damagecategory" -> it.damageCategory.displayName.string
                    "desc" -> it.description.string
                    else -> "UNKNOWN-PARAMETER-2"
                }.toString()
            } ?: "NO-MOVE"
        } ?: pokemon.moveSet.joinToString { it.displayName.string })
    }

    //状态
    val status: String by lazy { pokemon.status?.status?.showdownName ?: "NO-STATUS" }

    //数值
    //不是ivs也不是evs那个
    //可选参数 hp atk def spa spd spe (evasion accuracy(可能有))[生命,攻击,防御,特攻,特防,速度]
    //当乱写参数则是acc
    //不加参就是所有数值一起
    val stats: String by lazy {
        args.getOrNull(0)?.let {
            pokemon.getStat(Stats.getStat(it)).toString()
        } ?: listOf(
            pokemon.maxHealth,
            pokemon.attack,
            pokemon.defence,
            pokemon.specialAttack,
            pokemon.specialDefence,
            pokemon.speed
        ).joinToString()
    }

    //ivs
    //可选同上
    val ivs: String by lazy {
        args.getOrNull(0)?.let {
            pokemon.ivs[Stats.getStat(it)].toString()
        } ?: Stats.ALL.map { pokemon.ivs[it] }.joinToString()
    }

    //evs
    //可选同上
    val evs: String by lazy {
        args.getOrNull(0)?.let {
            pokemon.evs[Stats.getStat(it)].toString()
        } ?: Stats.ALL.map { pokemon.evs[it] }.joinToString()
    }

    //json解析
    //可选参数需要自己找了 可以先不写可选显示出所有键,键写法列如 [key].[subkey].[list].[index(int)].[key].[subkey]
    val json: String by lazy {
        args.getOrNull(0)?.let {
            pokemon.saveToJSON(RegistryAccess.EMPTY, JsonObject()).parse(it)
        } ?: GSON.toJson(pokemon.saveToJSON(RegistryAccess.EMPTY, JsonObject()))
    }

    //蛋组
    val egggroup: String by lazy {
        pokemon.species.eggGroups.joinToString {
            it.name
        }
    }

    //形态名
    val form: String by lazy {
        pokemon.form.name
    }

    //精灵所有形态的名聚合
    val forms: String by lazy {
        pokemon.species.forms.joinToString {
            it.name
        }
    }

    val biome: String by lazy {
        pokemon.getSpawnDetail()?.let {
            val list = mutableListOf<String>()
            for (condition in it.conditions) {
                condition.biomes?.forEach { condition ->
                    (condition as? BiomeTagCondition)?.tag?.location?.toString()?.let { str ->
                        list.add(str)
                    }
                }
            }
            if (list.isNotEmpty()) list.joinToString() else null
        } ?: "NO-FIND"
    }
}

private fun JsonObject.parse(key: String): String {
    val split = key.split(".")
    var el: JsonElement? = this
    for (subKey in split) {
        el = when (el) {
            null -> return "NO-DATA"
            asJsonObject -> asJsonObject[subKey]
            asJsonArray -> asJsonArray[subKey.toInt()]
            asJsonPrimitive -> return asString
            else -> return "NO-DATA"
        }
    }
    if (el == null||el.isJsonNull) return "NO-DATA"
    if (el.isJsonPrimitive) return el.asString
    return GSON.toJson(el)
}
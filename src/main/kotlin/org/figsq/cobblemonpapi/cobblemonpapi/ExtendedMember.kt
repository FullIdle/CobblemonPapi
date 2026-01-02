package org.figsq.cobblemonpapi.cobblemonpapi

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.PokemonStoreManager
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.Gson
import net.minecraft.ResourceLocationException
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method
import java.util.*

var getPartyMethod: Method? = null
var getPCMethod: Method? = null

val Player.party: PlayerPartyStore
    get() {
        val handle = this.getHandle()
        getPartyMethod =
            getPartyMethod ?: PokemonStoreManager::class.java.getDeclaredMethod("getParty", handle.javaClass)
        return getPartyMethod!!.invoke(Cobblemon.storage, handle) as PlayerPartyStore
    }

val Player.pc: PCStore
    get() {
        val handle = this.getHandle()
        getPCMethod = getPCMethod ?: PokemonStoreManager::class.java.getDeclaredMethod("getPC", handle.javaClass)
        return getPCMethod!!.invoke(Cobblemon.storage, handle) as PCStore
    }

fun Player.getPartyOrPC(partyOrPcName: String): PokemonStore<out StorePosition> {
    return when (partyOrPcName.lowercase()) {
        "party" -> this.party
        "pc" -> this.pc
        else -> throw IllegalArgumentException("Invalid partyOrPcName: $partyOrPcName")
    }
}

fun Player.getHandle(): Any {
    return this.javaClass.getDeclaredMethod("getHandle").invoke(this)
}

/*fun Any.info(msg: String) {
    COBBLEMONPAPI.logger.info(msg)
}*/

val COBBLEMONPAPI: Main
    get() = JavaPlugin.getPlugin(Main::class.java)

val GSON: Gson = Gson()

fun UUID.getOfflinePlayer(): OfflinePlayer? {
    return Bukkit.getOfflinePlayers().find { it.uniqueId == this };
}

val spawnDetailCache = mutableMapOf<Species, () -> PokemonSpawnDetail?>()

val Species.spawnDetail: PokemonSpawnDetail?
    get() {
        return spawnDetailCache.getOrPut(this) {
            val detail = CobblemonSpawnPools.WORLD_SPAWN_POOL.find {
                it is PokemonSpawnDetail && it.pokemon.matches(
                    this,
                    false
                )
            } as? PokemonSpawnDetail
            {
                detail
            }
        }()
    }

fun Pokemon.getSpawnDetail(): PokemonSpawnDetail? {
    return this.species.spawnDetail
}

/**
 * 源码来自PokemonProperties内
 * @see [PokemonProperties.commonMatches]
 * @param def 当参数内没有species时候使用该默认值
 * @return 是否匹配
 */
fun PokemonProperties.matches(pokemonSpecies: Species, def: Boolean): Boolean {
    this.species?.run {
        try {
            val species = if (this == "random") {
                PokemonSpecies.species.random()
            } else {
                PokemonSpecies.getByIdentifier(this.asIdentifierDefaultingNamespace()) ?: return@run
            }
            return pokemonSpecies == species
        } catch (_: ResourceLocationException) {
            return false
        }
    }
    return def
}
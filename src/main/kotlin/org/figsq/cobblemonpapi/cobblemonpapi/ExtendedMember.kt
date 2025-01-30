package org.figsq.cobblemonpapi.cobblemonpapi

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.PokemonStoreManager
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.google.gson.Gson
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
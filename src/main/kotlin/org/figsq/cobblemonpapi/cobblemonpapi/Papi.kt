package org.figsq.cobblemonpapi.cobblemonpapi

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.minecraft.core.RegistryAccess
import org.bukkit.OfflinePlayer
import kotlin.reflect.full.declaredMemberProperties

object Papi : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "cobblemonpapi"
    }

    override fun getAuthor(): String {
        return COBBLEMONPAPI.description.authors.joinToString()
    }

    override fun getVersion(): String {
        return COBBLEMONPAPI.description.version
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String {
        return (readPoke(player, params)?.let { pair ->
            pair.first?.let { poke ->
                val args = pair.second.removePrefix("_").split("_")
                PapiArgs::class.declaredMemberProperties.find {
                    it.name == args[0]
                }?.let {
                    it.get(PapiArgs(player, poke, args.drop(1).toTypedArray())) as String
                }
            } ?: "NO-DATA"
        } ?: "NO-POKEMON-DATA").let {value->
            var result = value
            COBBLEMONPAPI.papiResultReplace.forEach {
                result = result.replace(it.key, it.value)
            }
            result
        }
    }

    override fun persist(): Boolean {
        return true
    }

    private fun readPoke(offlinePlayer: OfflinePlayer?, params: String): Pair<Pokemon?, String>? {
        val player = offlinePlayer?.player
        val end = params.indexOf(':')
        val type = params.substring(0, end)
        val endD = params.indexOf('_')
        val data = params.substring(end + 1, endD)
        return when (type) {
            "storage" -> data.let { posData ->
                posData.substring(1, posData.length - 1).split(",").let {
                    val box = it[0].toInt()
                    val slot = it[1].toInt()
                    if (box > -1) player!!.pc[PCPosition(box, slot)]
                    else player!!.party[PartyPosition(slot)]
                } to params.substring(endD + 1)
            }

            "dex" -> data.toIntOrNull()?.let { dex ->
                PokemonSpecies.getByPokedexNumber(dex)?.create(1) to params.substring(endD + 1)
            }

            "species" -> PokemonSpecies.getByName(data.lowercase())?.create(1) to params.substring(endD + 1)

            "json" -> params.substring(end + 1).let {
                var i = 0
                it.forEachIndexed { index, c ->
                    if (c == '{') i++
                    if (c == '}') i--
                    if (i == 0) {
                        return Pokemon.loadFromJSON(
                            RegistryAccess.EMPTY,
                            GSON.fromJson(it.substring(0, index + 1), JsonObject::class.java)
                        ) to
                                it.substring(index + 1)
                    }
                }
                null
            }

            else -> null
        }
    }
}
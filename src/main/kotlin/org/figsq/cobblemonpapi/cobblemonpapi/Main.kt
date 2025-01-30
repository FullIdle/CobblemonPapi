package org.figsq.cobblemonpapi.cobblemonpapi

import org.bukkit.plugin.java.JavaPlugin

class Main:JavaPlugin(){
    override fun onEnable() {
        Papi.register()
        getCommand("cobblemonpapi")!!.setExecutor(this)
    }
}
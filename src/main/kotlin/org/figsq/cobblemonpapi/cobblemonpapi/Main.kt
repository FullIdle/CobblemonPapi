package org.figsq.cobblemonpapi.cobblemonpapi

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class Main:JavaPlugin(){
    lateinit var papiResultReplace:Map<String,String>

    override fun onEnable() {
        this.reloadConfig()
        Papi.register()
        getCommand("cobblemonpapi")!!.setExecutor(this)
    }

    override fun reloadConfig() {
        this.saveDefaultConfig()
        super.reloadConfig()

        papiResultReplace = config.getConfigurationSection("papi-result-replace")!!.let { section ->
            section.getKeys(false).associateWith {
                section.getString(it)!!
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.isOp) {
            sender.sendMessage("§cYou don't have permission to use this command.")
            return false
        }
        this.reloadConfig()
        sender.sendMessage("§aReloaded config.")
        return false
    }
}
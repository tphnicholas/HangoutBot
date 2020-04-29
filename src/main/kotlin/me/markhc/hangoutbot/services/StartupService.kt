package me.markhc.hangoutbot.services

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.launchMuteTimers
import me.markhc.hangoutbot.utilities.muteMemberWithTimer
import me.markhc.hangoutbot.utilities.unmuteMember
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import java.awt.Color

@Service
class StartupService(properties: Properties, guilds: GuildConfigurations, persistenceService: PersistenceService, discord: Discord, permissionsService: PermissionsService) {
    init {
        launchMuteTimers(guilds, persistenceService, discord)

        with(discord.configuration) {
            mentionEmbed = {
                embed {
                    val channel = it.channel
                    val self = channel.jda.selfUser

                    color = Color(0x00bfff)
                    thumbnail = self.effectiveAvatarUrl

                    field {
                        name = self.fullName()
                        value = "A bot to manage utility commands and funcionaility that does not warrant its own bot"
                    }
                    field {
                        name = "Prefix"
                        value = guilds.getGuildConfig(it.guild.id).prefix
                        inline = true
                    }
                    field {
                        name = "Contributors"
                        value = "markhc#0001"
                        inline = true
                    }

                    with (properties) {
                        val kotlinVersion = kotlin.KotlinVersion.CURRENT

                        field {
                            name = "Build Info"
                            value = "```"+
                                    "Version: $version\n" +
                                    "KUtils:  $kutils\n" +
                                    "Kotlin:  $kotlinVersion" +
                                    "```"
                        }

                        addField("Source", repository)
                    }
                }
            }


            visibilityPredicate = predicate@{ command: Command, user: User, _: MessageChannel, guild: Guild? ->
                guild ?: return@predicate false

                val member = user.toMember(guild)!!
                val permission = command.requiredPermissionLevel

                permissionsService.hasClearance(member, permission)
            }
        }
    }
}
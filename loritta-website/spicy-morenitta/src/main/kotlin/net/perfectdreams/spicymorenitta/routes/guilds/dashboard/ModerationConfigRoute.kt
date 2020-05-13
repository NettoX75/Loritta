package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import SaveStuff
import jQuery
import jq
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.p
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.Placeholders
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.js.Json
import kotlin.js.json

class ModerationConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/moderation") {
	override val keepLoadingScreen: Boolean
		get() = true
	val customPunishmentMessages = mutableListOf<ServerConfig.ModerationPunishmentMessageConfig>()

	@Serializable
	class PartialGuildConfiguration(
			val textChannels: List<ServerConfig.TextChannel>,
			val moderationConfig: ServerConfig.ModerationConfig
	)

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "textchannels", "moderation")
			switchContentAndFixLeftSidebarScroll(call)

			for (punishment in guild.moderationConfig.punishmentActions) {
				addPunishment(punishment)
			}

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-2")

			LoriDashboard.configureTextChannelSelect(jq("#punishmentLogChannelId"),  guild.textChannels, guild.moderationConfig.punishmentLogChannelId)

			jq(".add-new-action").click {
				addPunishment(
						ServerConfig.WarnAction(
								1,
								ServerConfig.PunishmentAction.BAN,
								null
						)
				)
			}

			LoriDashboard.configureTextArea(
					jq("#punishmentLogMessage"),
					true,
					null,
					true,
					jq("#punishmentLogChannelId"),
					true,
					Placeholders.DEFAULT_PLACEHOLDERS.toMutableMap().apply {
						put("reason", "Motivo da punição, caso nenhum motivo tenha sido especificado, isto estará vazio")
						put("punishment", "Punição aplicada (ban, mute, kick, etc)")
						put("staff", "Mostra o nome do usuário que fez a punição")
						put("@staff", "Menciona o usuário que fez a punição")
						put("staff-discriminator", "Mostra o discriminator do usuário que fez a punição")
						put("staff-id", "Mostra o ID do usuário que fez a punição")
						put("staff-avatar-url", "Mostra a URL do avatar do usuário que fez a punição")
					}
			)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val specificPunishmentMessages = document.select<HTMLDivElement>("#specific-punishment-messages")

			specificPunishmentMessages.append {
				div(classes = "flavourText") {
					+ locale["modules.moderation.specificMessagesForPunishment.title"]
				}

				locale.getList("modules.moderation.specificMessagesForPunishment.description").forEach {
					p {
						+ it
					}
				}

				select {
					id = "select-specific-message"

					for (entry in ServerConfig.PunishmentAction.values()) {
						option {
							value = entry.name
							+locale["commands.moderation.${entry.name.toLowerCase()}.punishAction"]
						}
					}
				}

				button(classes = "button-discord button-discord-info pure-button") {
					+ locale["loritta.add"]

					onClickFunction = {
						val specificMessageSelection = document.select<HTMLSelectElement>("#select-specific-message")
								.value

						val action = ServerConfig.PunishmentAction.valueOf(specificMessageSelection)

						val newEntry = ServerConfig.ModerationPunishmentMessageConfig(
								action,
								""
						)

						addCustomPunishmentMessage(newEntry)
					}
				}

				div(classes = "list-wrapper") {
					id = "specific-message-list"
				}
			}

			customPunishmentMessages.clear()
			guild.moderationConfig.punishmentMessages.forEach {
				addCustomPunishmentMessage(it)
			}
		}
	}

	fun addCustomPunishmentMessage(customPunishmentMessagesConfig: ServerConfig.ModerationPunishmentMessageConfig) {
		val specificPunishmentMessages = document.select<HTMLDivElement>("#specific-message-list")

		val action = customPunishmentMessagesConfig.action

		specificPunishmentMessages.append {
			div {
				div(classes = "flavourText") {
					locale.buildAsHtml(locale["modules.moderation.specificMessagesForPunishment.messageThatWillBeShown"], { num ->
						if (num == 0)
							span {
								+locale["commands.moderation.${action.name.toLowerCase()}.punishAction"]
							}
					}) { str ->
						+ str
					}
				}

				button(classes = "button-discord button-discord-info pure-button") {
					i(classes = "fas fa-trash") {}

					onClickFunction = {
						customPunishmentMessages.removeAll {
							it.action == action
						}

						(it.currentTarget as HTMLElement).parentElement!!.remove()
					}
				}

				textArea {
					id = "specific-$action-message"
					+(customPunishmentMessagesConfig.message)

					onInputFunction = {
						customPunishmentMessagesConfig.message = document.select<HTMLTextAreaElement>("#specific-$action-message").value
					}
				}

				hr {}
			}
		}

		LoriDashboard.configureTextArea(
				jq("#specific-$action-message"),
				true,
				null,
				false,
				null,
				true,
				Placeholders.DEFAULT_PLACEHOLDERS.toMutableMap().apply {
					put("reason", "Motivo da punição, caso nenhum motivo tenha sido especificado, isto estará vazio")
					put("punishment", "Punição aplicada (ban, mute, kick, etc)")
					put("staff", "Mostra o nome do usuário que fez a punição")
					put("@staff", "Menciona o usuário que fez a punição")
					put("staff-discriminator", "Mostra o discriminator do usuário que fez a punição")
					put("staff-id", "Mostra o ID do usuário que fez a punição")
					put("staff-avatar-url", "Mostra a URL do avatar do usuário que fez a punição")
				}
		)

		customPunishmentMessages.add(customPunishmentMessagesConfig)
	}

	fun addPunishment(warnAction: ServerConfig.WarnAction) {
		val action = jq("<div>")
				.append(
						jq("<button>")
								.attr("class", "button-discord button-discord-info pure-button remove-action")
								.html("<i class=\"fas fa-trash\"></i>")
				)
				.append(" Ao chegar em ")
				.append(
						jq("<input>")
								.attr("type", "number")
								.attr("min", 1)
								.`val`(warnAction.warnCount)
								.attr("class", "warnCount")
				).append(" avisos, ")
				.append("<select class='apply-punishment'>")
				.append(" o usuário")
				.append(jq("<div>")
						.css("height", "0px")
						.css("overflow", "hidden")
						.css("transition", "2s")
						.addClass("customMetadata")
						.append("O usuário deverá ser silenciado por ")
						.append(
								jq("<input>")
										.attr("type", "text")
										.attr("placeholder", "30 minutos")
										.`val`(warnAction.customMetadata0)
										.attr("class", "customMetadata0")
						)
				)

		if (warnAction.punishmentAction.toString() == ServerConfig.PunishmentAction.MUTE.toString()) {
			action.find(".customMetadata")
					.css("height", "48px")
		}

		jq("#warnActions").append(
				action
		)

		action.find(".remove-action").click {
			action.remove()
		}

		val applyPunishment = action.find(".apply-punishment")

		for (punishment in ServerConfig.PunishmentAction.values().filter { it.canChainWithWarn }) {
			val option = jq("<option>")
					.attr("name", legacyLocale[punishment.toString().replace("_", "") + "_PunishName"])
					.attr("value", punishment.toString())
					.text(legacyLocale[punishment.toString().replace("_", "") + "_PunishName"])

			if (warnAction.punishmentAction.toString() == punishment.toString()) {
				option.attr("selected", "selected")
			}

			applyPunishment.append(option)
		}

		jq(".apply-punishment").click {
			val punishmentAction = ServerConfig.PunishmentAction.valueOf(action.find(".apply-punishment").`val`().unsafeCast<String>())

			if (punishmentAction.toString() == ServerConfig.PunishmentAction.MUTE.toString()) {
				action.find(".customMetadata")
						.css("height", "48px")
			} else {
				action.find(".customMetadata")
						.css("height", "0px")
			}
		}
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("moderation", { payload ->
			val actions = mutableListOf<Json>()

			val warnActions = jq("#warnActions")

			val children = warnActions.children()

			children.each { index, elem ->
				val el = jQuery(elem)
				val json = json()

				val punishmentAction = ServerConfig.PunishmentAction.valueOf(el.find(".apply-punishment").`val`().unsafeCast<String>())
				json["punishmentAction"] = punishmentAction.toString()
				json["warnCount"] = el.find(".warnCount").`val`().unsafeCast<Int>()

				if (punishmentAction.toString() == ServerConfig.PunishmentAction.MUTE.toString())
					json["customMetadata0"] = el.find(".customMetadata0").`val`().unsafeCast<String>()

				actions.add(
						json
				)
			}

			val punishmentMessages = customPunishmentMessages.map {
				json().apply {
					this["action"] = it.action.toString()
					this["message"] = it.message
				}
			}

			payload["punishmentActions"] = actions
			payload["punishmentMessages"] = punishmentMessages
		})
	}
}
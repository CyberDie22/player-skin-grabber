package com.benbuzard.playerskingrabber

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.ai.TargetPredicate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.Base64


class PlayerSkinGrabber : ModInitializer {

    override fun onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
            dispatcher.register(
                ClientCommandManager.literal("grabskin").executes { context: CommandContext<FabricClientCommandSource> ->
                    val client = MinecraftClient.getInstance()
                    val world = client.world!!

                    val thisPlayer = context.source.player
                    val closestPlayer = world.getClosestPlayer(thisPlayer.x, thisPlayer.y, thisPlayer.z, 2.0) {
                        it.uuid != thisPlayer.uuid
                    }

                    val skin = closestPlayer?.gameProfile?.properties?.get("textures")?.stream()?.findAny()?.get()?.value
                    println("skin: $skin")
                    val decodedSkin = String(Base64.getDecoder().decode(skin))
                    println("decoded skin: $decodedSkin")
                    val url = Json.parseToJsonElement(decodedSkin).jsonObject["textures"]?.jsonObject?.get("SKIN")?.jsonObject?.get("url")?.jsonPrimitive?.content
                    println("url: $url")

                    context.source.sendFeedback(Text.literal("The URL for that players skin is: ").append(Text.literal(url).setStyle(
                        Style.EMPTY.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url)))))
//                    context.source.sendFeedback(Text.literal("The closest player to you's skinTexture is: ${closestPlayer?.gameProfile}"))
                    context.source.sendFeedback(Text.literal("Your skinTexture is: ${context.source.player.skinTexture.path}"))
                    0
                }
            )
        })
    }
}
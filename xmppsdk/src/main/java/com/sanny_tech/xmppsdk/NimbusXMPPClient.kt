package com.nimbus.sdk

import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.*
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.stringprep.XmppStringprepException
import java.util.concurrent.Executors

class NimbusXMPPClient(
    private val server: String,
    private val domain: String,
    private val username: String, // device ID
    private val password: String = "dispatcher-password", // or a token
    private val listener: NimbusListener
) {
    private lateinit var connection: AbstractXMPPConnection

    fun connect() {
        val jid = JidCreate.entityBareFrom("$username@$domain")

        val config = XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword(jid.localpart, password)
            .setXmppDomain(domain)
            .setHost(server)
            .setPort(9092)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
            .setSendPresence(true)
            .build()

        connection = XMPPTCPConnection(config)

        Thread {
            try {
                connection.connect().login()

                val chatManager = ChatManager.getInstanceFor(connection)
                chatManager.addIncomingListener { from, message, _ ->
                    listener.onMessageReceived(from.asBareJid().toString(), message.body)
                    // Optionally: send ACK here
                }

                listener.onConnected()
            } catch (e: Exception) {
                listener.onError(e)
            }
        }.start()
    }

    fun disconnect() {
        connection.disconnect()
    }

    fun sendMessage(to: String, message: String) {
        val jid = JidCreate.entityBareFrom(to)
        val chat = ChatManager.getInstanceFor(connection).chatWith(jid)
        chat.send(message)
    }
}

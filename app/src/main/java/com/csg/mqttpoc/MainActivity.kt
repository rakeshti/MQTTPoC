package com.csg.mqttpoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.csg.mqttpoc.databinding.ActivityMainBinding
import mqtt.MqttManagerImpl
import mqtt.MqttStatusListener
import org.eclipse.paho.client.mqttv3.MqttMessage

const val TAG = "msg"
//const val serverUri = "tcp://broker.hivemq.com:1883"
const val serverUri = "014311f85255424c871f629421d8cef9.s1.eu.hivemq.cloud"
const val subscriptionTopic = "messagesFromCroatia/#"
const val publishTopic = "messagesFromCroatia"
class MainActivity : AppCompatActivity() {
    private var clientId = "clientId-OwlWcC208Q"
    private lateinit var binding: ActivityMainBinding
    lateinit var mqttManager: MqttManagerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mqttManager = MqttManagerImpl(
            applicationContext,
            serverUri,
            clientId,
            arrayOf(subscriptionTopic),
            IntArray(1) { 0 })
        mqttManager.init()
        initMqttStatusListener()
        mqttManager.connect()

        binding.buttonSubmit.setOnClickListener {
            submitMessage()
        }
    }

    private fun initMqttStatusListener() {
        mqttManager.mqttStatusListener = object : MqttStatusListener {
            override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    displayInDebugLog("Reconnected to : $serverURI")
                } else {
                    displayInDebugLog("Connected to: $serverURI")
                }
            }

            override fun onConnectFailure(exception: Throwable) {
                displayInDebugLog("Failed to connect")
            }

            override fun onConnectionLost(exception: Throwable) {
                displayInDebugLog("The Connection was lost.")
            }

            override fun onMessageArrived(topic: String, message: MqttMessage) {
                displayInMessagesList(String(message.payload))
            }

            override fun onTopicSubscriptionSuccess() {
                displayInDebugLog("Subscribed!")
            }

            override fun onTopicSubscriptionError(exception: Throwable) {
                displayInDebugLog("Failed to subscribe")
            }

        }
    }

    private fun displayInMessagesList(message: String) {
        binding.textLogs.apply {
            setText(message + "\n" + text)
        }
    }

    private fun displayInDebugLog(message: String) {
        Log.i(TAG, message)
    }

    private fun submitMessage() {
        val message = binding.editTextMessage.text.toString()
        if (TextUtils.isEmpty(message)) {
            displayToast(R.string.general_please_write_some_message)
            return
        }
        mqttManager.sendMessage(message, publishTopic)
        clearInputField()
    }

    private fun clearInputField() {
        binding.editTextMessage.setText("")
    }

    private fun displayToast(@StringRes messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

}
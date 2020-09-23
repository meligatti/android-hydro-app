package com.example.hydroapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wifi_config.*
import java.io.IOException
import java.io.OutputStream
import java.util.*

// TENGO QUE AGREGAR CODIGO PARA ONRESUME (POR SI SALGO Y VUELVO)

class WifiConfigActivity : AppCompatActivity() {

    private val mBtoothObject = BtService(this, this@WifiConfigActivity)
    // Número aleatorio identificatorio de la app
    private val BT_APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var btSocket: BluetoothSocket
    private lateinit var btDevice: BluetoothDevice

    private var dataOut: OutputStream? = null
    private var devicesConnected: Boolean = false

    private val NOT_PRESSED: Int = 0
    private val NAME_PRESSED: Int = 1
    private val PASS_PRESSED: Int = 2

    private var sendCounter: Int = NOT_PRESSED

    /* NOTA: Tengo que mandar un mensaje de inicialización sí o sí porque si no, el ESP32 no sabe
       que le voy a mandar datos, y los pierdo. Quise hacerlo sacando ese estado en el ESP32 y
       mandando los datos de una, pero no funcionó, te muestra los carteles tarde. No recuerdo
       si funcionalmente pasaba algo, puede que el timer corriera desde que mandé el primer dato.*/
    private val initMsg = byteArrayOf(8, 0, 3, 0, 0)//String = "ready"//ByteArray = byteArrayOf(255.toByte(), 0.toByte(), 3.toByte(), 0.toByte(), 0.toByte(), 0.toByte())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_config)

        val devAddr: String? = intent.getStringExtra("addr")
        if (devAddr != null) {
            btDevice = mBtoothObject.getBtDevice(devAddr)
        }

        initRoutine()

        sendButton.setOnClickListener {
            sendCounter = sendCounter + 1
            changeScreenText()
            var msg: String = messageEditText.text.toString()
            mBtoothObject.sendMessage(msg)
            checkTransmissionState()
        }
    }

    /*override fun onResume() {
        super.onResume()
        initRoutine()
    }*/

    override fun onPause() {
        super.onPause()
        recreate()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
        //initRoutine()
    }

    private fun checkTransmissionState() {
        when (sendCounter) {
            NAME_PRESSED -> clearBoxText()
            PASS_PRESSED -> returnToMainActivity()
        }
    }

    private fun returnToMainActivity() {
        var closeSuccess: Boolean = true

        try {
            btSocket.close()
        } catch (ioe: IOException) {
            println("Problem releasing socket")
            closeSuccess = false
        }

        if (closeSuccess) {
            val successIntent = Intent()
            setResult(Activity.RESULT_OK, successIntent)
            finish()
        }
    }

    private fun initRoutine() {
        //val selectedDevice = getDevice()
        val connSuccess: Boolean = mBtoothObject.establishBtConnection()
        if(connSuccess) {
            devicesConnected = true
            btSocket = mBtoothObject.getBtSocket()
            mBtoothObject.setupInputStream(btSocket)
            // La conversión del siguiente renglón NO FUNCA:
            // mBtoothObject.sendMessage(initMsg.toString())
            mBtoothObject.sendMessage(String(initMsg))
        } else {
            failedReturn()
        }
    }

    private fun clearBoxText() {
        messageEditText.text.clear()
    }

    
    private fun failedReturn() {
        // Código extra para retornar a la main activity
       /* val connFail = Intent()
        setResult(Activity.RESULT_CANCELED, connFail)
        finish()*/
        setResult(Activity.RESULT_CANCELED)
        finish()
    }


    private fun changeScreenText() {
        instructionWifiTextview.text = "Insert the password of your network"
    }

}
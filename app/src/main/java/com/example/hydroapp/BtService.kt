package com.example.hydroapp

import android.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bt.*
import kotlinx.android.synthetic.main.activity_wifi_config.*
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BtService {

    private lateinit var context: Context
    private lateinit var act: Activity

    private lateinit var adapter: BluetoothAdapter
    private lateinit var devicesArrayAdapter: ArrayAdapter<String>
    private lateinit var pairedDevicesList: MutableSet<BluetoothDevice>
    private lateinit var listView: ListView

    private val BT_APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var selectedDevice: BluetoothDevice
    private lateinit var btSocket: BluetoothSocket

    private var dataOut: OutputStream? = null

    constructor(ctxt: Context, activ: Activity) {
        this.context = ctxt
        this.act = activ
        this.adapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun getBtAdapter() : BluetoothAdapter{
        return adapter
    }

    fun getBondedDevices() : MutableSet<BluetoothDevice> {
        return adapter.bondedDevices
    }

    fun turnOnBluetooth() {
        if (adapter == null) {
            Toast.makeText(context, "No Bluetooth Adapter Available", Toast.LENGTH_LONG)
        } else {
            // Non null call (!!)
            if(!adapter!!.isEnabled) {
                val enableBTintent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBTintent)
            }
        }
    }

    /* Funciones usadas en la actividad de Wifi Config*/

    fun getBtDevice(devAddr: String) : BluetoothDevice{
        selectedDevice = adapter.getRemoteDevice(devAddr)
        return selectedDevice
    }

    fun setBtDevice(devAddr: String) {
        selectedDevice = adapter.getRemoteDevice(devAddr)
    }

    fun getBtSocket() : BluetoothSocket{
        return btSocket
    }

    fun establishBtConnection(): Boolean {

        var connectionDone: Boolean = true
        var connectionClosed: Boolean = true

        var connSuccess: Boolean

        var connectionCreated: Boolean

        // Intento crear el Socket para conectarme
        var socketDone: Boolean = createSocket(selectedDevice)
        if (socketDone) {
            // Intento conectarme al dispositivo
            connectionCreated = createConnection()
        } else {
            connectionCreated = false
        }

        connSuccess = socketDone and connectionCreated
        if (connSuccess) {
            /*println("Successful connection")
            Toast.makeText(act, "Successful connection", Toast.LENGTH_LONG)
                .show()*/
        } else if (!socketDone) {
            println("Error creating the socket")
        } else if (!connectionCreated) {
            println("Error during connection")

        }

        return connSuccess
    }

    private fun createSocket(selectedDevice: BluetoothDevice): Boolean {
        var socketDone: Boolean = true
        // Intento crear el Socket para conectarme
        try {
            btSocket = selectedDevice.createRfcommSocketToServiceRecord(BT_APP_UUID)
            println("Trying to create Socket")
        } catch (socketErr: IOException) {
            socketDone = false
            Toast.makeText(act, "Socket creation has failed", Toast.LENGTH_LONG).show()
        }
        return socketDone
    }

    private fun closeSocket() {
        try {
            btSocket.close()
            println("Attempting to close connection")
        } catch(socketCloseErr: IOException) {
            Toast.makeText(act, "Unable to close connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun createConnection(): Boolean {
        /* NOTA: Después de establecer una conexión tengo que cerrarla porque
         eso habilita a que se pueda conectar mas de un equipo a la vez */
        var connectionDone: Boolean = true
        var connectionClosed: Boolean = true
        try {
            btSocket.connect()
            println("Attempting to create connection")
        } catch (connError: IOException) {
            connectionDone = false
            closeSocket()
            /*try {
                btSocket.close()
                println("Trying to close connection")
            } catch (closeError: IOException) {
                connectionClosed = false
                println("Unable to close connection")
                Toast.makeText(act, "Something went wrong", Toast.LENGTH_LONG).show()
            }*/
        }
        return (connectionClosed and connectionDone)
    }

    fun closeConnection() {
        closeSocket()
    }

    fun setupInputStream(mSocket: BluetoothSocket?) {
        var dataToSend: OutputStream? = null
        try {
            dataToSend = mSocket!!.outputStream
        } catch (ioe: IOException) {

        }
        dataOut = dataToSend
    }

    fun sendMessage(msg: String) {
        val msgBuffer = msg.toByteArray() + 0.toByte()
        try {
            dataOut!!.write(msgBuffer)
        } catch (ioe: IOException) {
            Toast.makeText(act, "Error sending data", Toast.LENGTH_LONG).show()
            // Poner intent para volver a la actividad anterior
            closeSocket()
        }
    }

}
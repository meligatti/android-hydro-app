package com.example.hydroapp

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
//import com.example.hydroapp.R.*
import kotlinx.android.synthetic.main.activity_bt.*
import kotlinx.android.synthetic.main.activity_main.*

class BtActivity : AppCompatActivity() {
    // Declaración de variables sin inicializar
    private var mBtObject = BtService(this, this@BtActivity)
    private lateinit var devicesArrayAdapter: ArrayAdapter<String>
    private lateinit var pairedDevicesList: MutableSet<BluetoothDevice>
    private lateinit var listView: ListView

    //@SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt)

        createPairedDevicesList()

    pairedDevicesLv.setOnItemClickListener { _, _, position, _ ->
        /* Nota: como estoy poniendo al deviceBundle como constante, puede que no me pueda conectar
        al dispositivo correcto si le pifio al apretar en el menú. Probar.
        Rta: esto parece no cumplirse. */
        getSelectedDevice(position)
    }
    }

    override fun onPause() {
        super.onPause()
        recreate()
    }

    private fun getSelectedDevice(position: Int) {
        val deviceBundle: Bundle = Bundle()
        deviceBundle.putString("name", pairedDevicesList.elementAt(position).name)
        deviceBundle.putString("address", pairedDevicesList.elementAt(position).address)

        returnDevice(deviceBundle)
    }

    private fun returnDevice(deviceBundle: Bundle) {
        val returnPairedDevice = Intent()
        returnPairedDevice.putExtra("deviceData", deviceBundle)
        setResult(Activity.RESULT_OK, returnPairedDevice)
        finish()
    }

    private fun createPairedDevicesList() {
        if (mBtObject.getBtAdapter() == null) {
            Toast.makeText(this, "Bluetooth adapter is not working", Toast.LENGTH_LONG).show()
        } else {
            if (mBtObject.getBtAdapter().isEnabled) {
                pairedDevicesList = mBtObject.getBondedDevices()
                // Codigo a testear en un celular que no tiene dispositivos vinculados
                if(pairedDevicesList.size == 0) {
                    noDevicesReturn()
                } else {
                    showDevicesList()
                }
            }
        }
    }

    private fun noDevicesReturn() {
        Toast.makeText(this, "No paired devices", Toast.LENGTH_LONG).show()
        val noPairedDevices = Intent()
        setResult(Activity.RESULT_CANCELED, noPairedDevices)
        finish()
    }

    private fun showDevicesList() {
        val devicesArray = arrayOfNulls<String>(pairedDevicesList.size)
        for (i in 0 until pairedDevicesList.size) {
            devicesArray[i] = pairedDevicesList.elementAt(i).name + "\n" + pairedDevicesList.elementAt(i).address
        }
        devicesArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devicesArray)
        // Nota: estoy probando sin crear una variable listview, funca
        pairedDevicesLv.adapter = devicesArrayAdapter
    }

}
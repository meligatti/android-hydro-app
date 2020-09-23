package com.example.hydroapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.icu.text.Transliterator
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bt.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //var BtService: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var BtoothAdapter: BtService = BtService(this, this@MainActivity)

    // Codes that help to recognize which activity has finished
    val REQUEST_CODE_ENABLE_BT: Int = 1
    val REQUEST_CODE_PAIRED_DEVICE: Int = 2
    val REQUEST_CODE_WIFI_CONFIG: Int = 3

    private var deviceMACaddr: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* Llamo al constructor y creo una instancia BtService.
        * Luego, le indico que prendo el bluetooth -> Desaparezco checkBtServiceExistence() */
        BtoothAdapter.turnOnBluetooth()

        //checkBtServiceExistence()

        // When clicking the BTButton, it changes to BtActivity which has a list of all paired
        // devices
        BTButton.setOnClickListener {
            if (BtoothAdapter.getBtAdapter() == null) {
                Toast.makeText(this, "No Bluetooth Adapter Available", Toast.LENGTH_LONG)
            } else {
                val BTintent = Intent(this, BtActivity::class.java)
                startActivityForResult(BTintent, REQUEST_CODE_PAIRED_DEVICE)
            }
        }

        // Pressing WiFiButton, the program advances to WiFiConfigActivity using the MAC address
        // of the selected Bluetooth device.
        WiFiButton.setOnClickListener {
            if(deviceMACaddr.isEmpty()){
                Toast.makeText(this, "No selected device", Toast.LENGTH_LONG).show()
            } else {
                val devAddr: String = deviceMACaddr
                val WifiConfigIntent = Intent(this, WifiConfigActivity::class.java)
                WifiConfigIntent.putExtra("addr", devAddr)
                startActivityForResult(WifiConfigIntent, REQUEST_CODE_WIFI_CONFIG)
            }
        }
    }

    // This method is called when the program flow returns from the other activities
    // When the user selects any Bluetooth device to connect, the program stores its name and
    // its MAC address and displays them on the bottom of the screen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_PAIRED_DEVICE) and (resultCode == Activity.RESULT_OK)) {
            val deviceBundle = data!!.getBundleExtra("deviceData")
            val deviceName = deviceBundle!!.getString("name")
            val deviceAddr = deviceBundle!!.getString("address")
            deviceMACaddr = deviceAddr!!
            posTextView.text = deviceName
        } else if ((requestCode == REQUEST_CODE_WIFI_CONFIG) and (resultCode == Activity.RESULT_CANCELED)) {
            Toast.makeText(this@MainActivity, "The connection was aborted", Toast.LENGTH_LONG)
            println("Connection couldn't be completed")
        }
    }

}
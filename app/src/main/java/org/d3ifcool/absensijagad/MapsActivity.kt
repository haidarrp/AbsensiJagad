package org.d3ifcool.absensijagad

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.logout_btn
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var newestCoord:LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getMapUpdate()
        refresh_btn.setOnClickListener {
            getMapUpdate()
        }
        logout_btn.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()

                } else
                    Toast.makeText(this, "Logout Succesfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
    private fun getMapUpdate(){
        val rootRef = FirebaseDatabase.getInstance().reference
        val ref= rootRef.child("USERS")
        val valueEventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    val latitude = ds.child("lat").getValue(Double::class.java)
                    val longitude = ds.child("lng").getValue(Double::class.java)
                    val name = ds.child("name").getValue(String::class.java)
                    Log.d("getDataCoor","latitude= "+latitude+" longitude: "+longitude)
                    val userCoord = LatLng(latitude!!, longitude!!)
                    newestCoord=userCoord
                    mMap.addMarker(MarkerOptions()
                        .position(userCoord)
                        .title(name)
                        )
                }
                val zoomLevel = 15f
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newestCoord,zoomLevel))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAGerror", error.getMessage())
            }
        }
        ref.addListenerForSingleValueEvent(valueEventListener)
    }
}
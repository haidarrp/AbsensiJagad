package org.d3ifcool.absensijagad

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity(){
    lateinit var ref : DatabaseReference
    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null
    private lateinit var loading:ProgressBar
    private lateinit var submit:Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var latitudeT: Double = -6.9527386
    private var longitudeT: Double = 107.6651714
    private var resultInMeter=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ref = FirebaseDatabase.getInstance().getReference("USERS")
        capture_btn.setOnClickListener {
            //if system os is Marshmallow or Above, we need to request runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                    //permission was not enabled
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //show popup to request permission
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else{
                    //permission already granted
                    openCamera()
                }
            }
            else{
                //system os is < marshmallow
                openCamera()
            }
        }
        submit_btn.setOnClickListener {
            if (resultInMeter>100){
                Toast.makeText(this, "!!DILUAR JANGKAUAN!!" +
                        "Gagal menginput data", Toast.LENGTH_SHORT).show()
            }else{
                image_uri?.let { it1 -> uploadImageToFirebase(it1) }
            }


        }
        logout_btn.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)

                } else
                    Toast.makeText(this, "Logout Succesfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        getLocation_btn.setOnClickListener {
            getLastKnownLocation()
        }
    }
    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION);
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    latitude = location.latitude
                }
                if (location != null) {
                    longitude = location.longitude
                }
                var lon1 = Math.toRadians(longitudeT)
                var lon2 = Math.toRadians(longitude)
                var lat1 = Math.toRadians(latitudeT)
                var lat2 = Math.toRadians(latitude)

                var dlon = lon2 - lon1
                var dlat = lat2 - lat1

                var a = Math.pow(Math.sin(dlat / 2), 2.0)+ Math.cos(lat1) * Math.cos(lat2)* Math.pow(Math.sin(dlon / 2), 2.0)
                var c = 2 * Math.asin(Math.sqrt(a))
                var r = 6371
                var result = c*r
                resultInMeter = result*1000

                if (resultInMeter>= 100){
                    Log.d("statusJarak","Gagal")
                }else
                    Log.d("statusJarak","Berhasil")

                Log.d("userlocation", "Latitude: "+latitude+" Longtitude: "+longitude)
                Log.d("Distance","jarak = "+resultInMeter+" M")
            }

    }
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            }
        }
        if (requestCode == PERMISSION_CODE ){
            if (grantResults.size > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK){
            //set image captured to image view
            image_view.setImageURI(image_uri)
        }
    }
    private fun uploadImageToFirebase(fileUri: Uri) {
        if (fileUri != null) {
            submit=findViewById(R.id.submit_btn)
            submit.visibility=View.INVISIBLE
            loading=findViewById(R.id.loading_panel)
            loading.visibility=View.VISIBLE
            val fileName = UUID.randomUUID().toString() +".jpg"

            val database = FirebaseDatabase.getInstance()
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            refStorage.putFile(fileUri)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val imageUrl = it.toString()
                            val desc = editTextDescription.text.toString()
                            val loggedInUser = FirebaseAuth.getInstance().currentUser
                            loggedInUser?.let {
                                val email = loggedInUser.email
                            }
                            val email = loggedInUser?.email
                            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                            val currentDate = sdf.format(Date()).toString()
                            val user = Karyawan(imageUrl, desc, email , currentDate)
                            Log.d("cekpush", email+" "+currentDate)
                            val userId = ref.push().key.toString()

                            ref.child(userId).setValue(user).addOnCompleteListener {
                                submit=findViewById(R.id.submit_btn)
                                submit.visibility=View.VISIBLE
                                loading=findViewById(R.id.loading_panel)
                                loading.visibility=View.INVISIBLE
                                Toast.makeText(this, "Successs",Toast.LENGTH_SHORT).show()
                                editTextDescription.setText("")
                                image_view.setImageURI(null)
                                image_view.setImageDrawable(
                                    ContextCompat.getDrawable(
                                    applicationContext, // Context
                                    R.drawable.ic_baseline_image_24 // Drawable
                                ))
                            }
                        }
                    })

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })
        }
    }
}
package org.d3ifcool.absensijagad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            // Check if user's email is verified
            val uid = user.uid
            val emailEt= findViewById(R.id.email_edt_text) as EditText
            val nameEt = findViewById(R.id.name_edt_text) as EditText
            emailEt.setText(email)
            nameEt.setText(name)
        }
        update_btn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val displayName = findViewById(R.id.name_edt_text) as EditText
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.text.toString())
                .build()
            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Update Succesfully", Toast.LENGTH_SHORT).show()
                        Log.d("TAG", "User profile updated.")
                        Log.d("UpdateName",displayName.text.toString())
                    }
                }

        }
    }

}
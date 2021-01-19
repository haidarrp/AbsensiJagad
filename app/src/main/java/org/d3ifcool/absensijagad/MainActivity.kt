package org.d3ifcool.absensijagad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        login.setOnClickListener {
            val emailTxt = findViewById<View>(R.id.editTextTextEmailAddress) as EditText
            var email = emailTxt.text.toString()
            val passwordTxt = findViewById<View>(R.id.editTextTextPassword) as EditText
            var password = passwordTxt.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {

                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else
                        Toast.makeText(this, "Succesfully Login", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed Login: ${it.message}")
                    Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
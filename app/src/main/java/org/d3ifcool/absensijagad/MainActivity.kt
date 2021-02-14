package org.d3ifcool.absensijagad

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loggedInUser = FirebaseAuth.getInstance().currentUser
        val uid=loggedInUser?.uid.toString()
        if (loggedInUser!= null){
            if (uid!="NMYBnrIESFbQzeCFlEuIznHi4lg2"){
                val intent = Intent(this, Dashboard::class.java)
                finish()
                startActivity(intent)
            }else{
                val intent = Intent(this, MapsActivity::class.java)
                finish()
                startActivity(intent)

            }
        }
        registerTxt.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        })
        login.setOnClickListener {
            val emailTxt = findViewById<View>(R.id.editTextTextEmailAddress) as EditText
            var email = emailTxt.text.toString()
            val passwordTxt = findViewById<View>(R.id.editTextTextPassword) as EditText
            var password = passwordTxt.text.toString()
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            }else{
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val loggedInUser = FirebaseAuth.getInstance().currentUser
                            val uid=loggedInUser?.uid.toString()
                            Log.d("uidUser","uid-nya="+uid)
                            Toast.makeText(this, "Succesfully Login", Toast.LENGTH_SHORT).show()
                            if (uid!="NMYBnrIESFbQzeCFlEuIznHi4lg2"){
                                val intent = Intent(this, Dashboard::class.java)
                                finish()
                                startActivity(intent)
                            }else{
                                val intent = Intent(this, MapsActivity::class.java)
                                finish()
                                startActivity(intent)
                            }
                        } else{
                        return@addOnCompleteListener
                        val intent = Intent(this, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        Log.d("Main", "Failed Login: ${it.message}")
                        Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


}
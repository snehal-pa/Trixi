package com.example.trixi.ui.login

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trixi.NetworkStateReceiver
import com.example.trixi.R
import com.example.trixi.apiService.RetrofitClient
import com.example.trixi.entities.User
import com.example.trixi.repository.PostToDb
import com.example.trixi.ui.register.RegisterActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), NetworkStateReceiver.ConnectivityReceiverListener {

    val post = PostToDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.activity_login)
        RetrofitClient.context = this

        post.GetLoggedInUserFromDB(this)

        registerReceiver(
            NetworkStateReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        login_no_account.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_button.setOnClickListener {
            val usernameOrEmail = login_input_username_or_password.text.toString()
            val password = login_input_password.text.toString()

            if (usernameOrEmail.contains("@")) {
                val user = User("", "", usernameOrEmail, password, "", "", "", null, null,null,null,null)
                post.PostLoginUserToDb(user, this)
            } else {
                val user = User("", usernameOrEmail, "", password, "", "", "", null, null,null,null,null)
                post.PostLoginUserToDb(user, this)
            }
        }
    }

    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            Toast.makeText(
                applicationContext,
                "Internet Not Available",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.d("networkaccess", "connected")
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }

    override fun onResume() {
        super.onResume()
        NetworkStateReceiver.connectivityReceiverListener = this
    }

}




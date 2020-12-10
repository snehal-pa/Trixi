package com.example.trixi.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.trixi.R
import com.example.trixi.apiService.RetrofitClient
import com.example.trixi.entities.User
import com.example.trixi.repository.GetFromDbViewModel
import com.example.trixi.repository.PostToDb
import com.example.trixi.ui.register.RegisterActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val model: GetFromDbViewModel by viewModels()
    val post = PostToDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        RetrofitClient.context = this
        post.GetLoggedInUserFromDB(this)

        login_no_account.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_button.setOnClickListener{
            val usernameOrEmail = login_input_username_or_password.text.toString()
            val password = login_input_password.text.toString()

            if (usernameOrEmail.contains("@")){
                val user = User("","", usernameOrEmail, password, "","","",null, null)
                post.PostLoginUserToDb(user, this)
            }else{
                val user = User("",usernameOrEmail, "", password, "","","",null, null)
                post.PostLoginUserToDb(user, this)
            }


        }


        //testObserver getAllUsers:
        /*model.GetAllUsersFromDB()
        model.getUserMutableLiveDataList().observe(this, Observer{
            it.forEach{
                Log.d("uus", "UserName : ${it.userName!!}")
            }
        } )*/

    }

}
package com.example.trixi.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.trixi.MainActivity
import com.example.trixi.apiService.Api
import com.example.trixi.apiService.RetrofitClient
import com.example.trixi.entities.Post
import com.example.trixi.entities.User
import com.example.trixi.ui.login.LoginActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostToDb {

    companion object {
        var loggedInUser: User? = null
        var latestPost: Post? = null
    }

    fun PostLoginUserToDb(user: User, context: Context){
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.loginUser(user)
        call?.enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("user", "login-user : onfailure: " + t.message)
                Log.d("user", "login-user : onfailure: Username/email does not exist")
                Toast.makeText(context, "Username/email does not exist", Toast.LENGTH_LONG).show()
            }
            override fun onResponse(
                call: Call<User>, response: Response<User>
            ) {
                if(response.isSuccessful){

                    Log.d("user", "login-user : onResponse success" + response.message())

                    GetLoggedInUserFromDB(context)
                }else{
                    Log.d("user", "login-user : onResponse else: password and username/email dont match")
                    Toast.makeText(context, "Password and username/email dont match", Toast.LENGTH_LONG).show()
                }
            }
        })

    }

    fun GetLoggedInUserFromDB(context: Context){
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.getLoggedInUser()
        call?.enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("uus", "loggedInUser : onfailure " + t.message)
            }
            override fun onResponse(
                call: Call<User>, response: Response<User>
            ) {
                if (response.isSuccessful) {
                    Log.d("loggedInUser", "User logged in successfully")
                    loggedInUser = response.body()
                    Log.d("loggedInUser", loggedInUser.toString())

                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)

                } else {
                    Log.d("loggedInUser", "User failed to login")
                }
            }
        })
    }



    fun PostImageToDb(image: String) {
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.postProfileImage(image)

        call?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //Log.d("user", "User: onfailure " + t.message)
            }
            override fun onResponse(
                    call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if(response.isSuccessful){
                    //Log.d("user", "User : onResponse success" + response.message())
                }else{
                    //Log.d("user", "User : onResponse else, failed" + response.message())
                }
            }
        })
    }
     fun PostRegisterUserToDb(user: User, context: Context){
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.createUser(user)
     call?.enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("uus", "REG-user : onfailure " + t.message)
            }
            override fun onResponse(
                call: Call<User>, response: Response<User>
            ) {
                if(response.isSuccessful){
                    Log.d("uus", "REGuser : onResponse success" + response.body())
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }else{
                    Log.d("uus", "REGuser : onResponse else" + response.body())
                }
            }
        })
    }

    fun logOutUser(context: Context?) {
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.logOutUser()
        call?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("logout", "logout user : onfailure " + t.message)
            }
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Log.d("logout", "User logged out successfully" + response.message())
                    val intent = Intent(context, LoginActivity::class.java)
                    context!!.startActivity(intent)

                } else {
                    Log.d("logout", "User failed to logout")
                }
            }
        })
    }


    fun sendPostToDb(post: Post) {

        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.postPostToDb(post)


        call?.enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.d("post", "Post : onfailure " + t.message)

            }
            override fun onResponse(
                call: Call<Post>, response: Response<Post>
            ) {
                if(response.isSuccessful){
                    Log.d("post", "Post : onResponse success" + (response.body()!!.uid  ))
                }else{
                    Log.d("post", "Post : onResponse else" + response.body()!!.uid)
                }
            }

        })

    }




}
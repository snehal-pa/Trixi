package com.example.trixi.repository

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trixi.apiService.Api
import com.example.trixi.apiService.RetrofitClient
import com.example.trixi.entities.Post
import com.example.trixi.entities.User
import com.example.trixi.ui.register.RegisterActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetFromDbViewModel : ViewModel() {

     var userListData: MutableLiveData<List<User>>
     var followingsPost :MutableLiveData<List<Post>>




    init {
        userListData = MutableLiveData()
        followingsPost = MutableLiveData()
    }

    fun getUserMutableLiveDataList(): MutableLiveData<List<User>> {
        return userListData
    }


    fun GetAllUsersFromDB(){
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)


        val call = retrofitClient?.getAllUsers()
        call?.enqueue(object : Callback<List<User>>{
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d("uus", "users : onfailure " + t.message)
                userListData.postValue(null)
            }
            override fun onResponse(
                call: Call<List<User>>, response: Response<List<User>>) {
                if(response.isSuccessful){
                    userListData.postValue(response.body())
                }else{
                    userListData.postValue(null)
                }
            }
        })
    }

    fun getFollowingsPostFromDb(id:String?):MutableLiveData<List<Post>>{
        val retrofitClient = RetrofitClient.getRetroInstance()?.create(Api::class.java)

        val call = retrofitClient?.getFollowingsPost(id)
        call?.enqueue(object :Callback<List<Post>>{
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("post", "posts : onfailure " + t.message)
                followingsPost.postValue(null)
            }

            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if(response.isSuccessful){
                    followingsPost.postValue(response.body())

                }else{
                    followingsPost.postValue(null)
                }
            }
        })
        return followingsPost;

    }


}
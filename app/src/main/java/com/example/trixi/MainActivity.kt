package com.example.trixi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.trixi.entities.Post
import com.example.trixi.repository.DataViewModel
import com.example.trixi.repository.PostToDb
import com.example.trixi.ui.discover.ShowTopPostsFragment
import com.example.trixi.ui.fragments.UploadFragment
import com.example.trixi.ui.fragments.singlePostFragment
import com.example.trixi.ui.home.HomepageFragment
import com.example.trixi.ui.profile.LoggedInUserProfileFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val db = PostToDb()


    //val model: DataViewModel by viewModels()

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
         //RetrofitClient.context = this

        //setContentView(R.layout.fragment_share)
        val single = intent.getStringExtra("EXTRA")

         val homepageFragment = HomepageFragment()
         val postFragment = UploadFragment()
         val discoverFragment = ShowTopPostsFragment()
         val profileFragment = LoggedInUserProfileFragment()
         val singleFragment = singlePostFragment()

         //val post = PostToDb.latestPost

         //Log.d("post", " $post")


         //print("main login-user :${PostToDb.loggedInUser}")

//         val bundle: Bundle = Bundle()
//         model.GetLoggedInUserFromDB().observe(this,{
//             if (it != null) {
//                 Log.d("uus","log in user from main activity. ${it.userName}")
//                 Log.d("uus","log in userId from main activity. ${it.uid}")
//
//
//                 bundle.putString("loggedInUserId", it.uid); }
//         })
//
//         homepageFragment.arguments = bundle

         if(single.isNullOrEmpty()){
             makeCurrentFragment(homepageFragment)
         } else {
            val bundle = Bundle()
             bundle.putString("EXTRA", "NewPost")
             singleFragment.arguments = bundle;
             makeCurrentFragment(singleFragment);
         }

         //makeCurrentFragment(homepageFragment)

         bottom_nav.setOnNavigationItemSelectedListener {
             when(it.itemId){
                 R.id.footer_home -> makeCurrentFragment(homepageFragment)
                 R.id.footer_search -> makeCurrentFragment(discoverFragment)
                 R.id.footer_post -> makeCurrentFragment(postFragment)
                 R.id.footer_profile -> makeCurrentFragment(profileFragment)
             }
             true
         }

    }




    fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container,fragment)
            commit()
        }
    }



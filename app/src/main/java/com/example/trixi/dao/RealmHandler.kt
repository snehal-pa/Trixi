package com.example.trixi.dao

import android.util.Log
import com.example.trixi.apiService.Api
import com.example.trixi.apiService.RetrofitClient
import com.example.trixi.entities.*
import io.realm.Realm
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RealmHandler(realm: Realm) {

    val service = RetrofitClient.getRetroInstance()?.create(Api::class.java)

    companion object {
        val realm = Realm.getDefaultInstance()
    }

    fun getAllUsersFromDB() {

        val call = service?.getAllUsers()
        call?.enqueue(object : Callback<List<User>> {
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d("uus", "users : onfailure " + t.message)
            }

            override fun onResponse(
                    call: Call<List<User>>, response: Response<List<User>>
            ) {
                if (response.isSuccessful) {
                    saveAllUsersToRealm(response.body()!!)
                } else {

                }
            }
        })
        return
    }


    private fun saveAllUsersToRealm(users: List<User>) {
        realm.executeTransactionAsync(fun(realm: Realm) {
            users.forEach { u ->
               val user = RealmUser().apply {
                    //realm = respones.body
                    uid = u.uid
                    userName = u.userName
                    email = u.email
                    bio = u.bio
                    imageUrl = u.imageUrl
                    role = u.role

                    pets?.addAll(u.pets!!.map {
                        RealmPet().apply {
                            uid = it.uid
                            ownerId = it.ownerId
                            name = it.name
                            imageUrl = it.imageUrl
                            age = it.age
                            bio = it.bio
                            breed = it.breed
                            gender = it.gender
                        }
                    })

                    posts?.addAll(u.posts!!.map {
                        RealmPost().apply {
                            uid = it.uid
                            title = it.title
                            description = it.description
                            filePath = it.filePath
                            ownerId = it.ownerId
                            comments?.addAll(it.comments!!.map {
                                RealmComment().apply {
                                    comment = it.comment
                                    userId = it.userId
                                    postId = it.postId
                                }
                            })
                            likes?.addAll(it.likes!!.map {
                                RealmLike().apply {
                                    userId = it.userId
                                    postId = it.postId
                                }
                            })
                        }
                    })

                    followers?.addAll(u.followers!!.map {
                        RealmUser().apply {
                            uid = it.uid
                            userName = it.userName
                        }
                    })
                    followingsUser?.addAll(u.followingsUser!!.map {
                        RealmUser().apply {
                            uid = it.uid
                            userName = it.userName
                        }
                    })
                    followingsPet?.addAll(u.followingsPet!!.map {
                        RealmPet().apply {
                            uid = it.uid
                            name = it.name
                        }
                    })
                }
                realm.insertOrUpdate(user)
            }
        }, fun() {
            Log.d("realm", "user uploaded to realm")
        })
    }

    fun getUserPostsFromDb(id: String) {
        val call = service?.getPostByOwnerId(id)
        call?.enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("posts", "posts : onfailure " + t.message)
            }
            override fun onResponse(
                    call: Call<List<Post>>, response: Response<List<Post>>
            ) {
                if (response.isSuccessful) {
                    saveUserPost(response.body()!!)
                } else {

                }
            }
        })
        return
    }

    private fun saveUserPost(posts: List<Post>) {
        realm.executeTransactionAsync(fun(realm: Realm) {

            var userPosts = RealmUserPost().apply {
                listUserId = 1
                userAllPosts?.addAll(posts.map {
                    RealmPost().apply {
                        //realm = respones.body
                        uid = it.uid
                        title = it.title
                        description = it.description
                        filePath = it.filePath
                        ownerId = it.ownerId
                        comments?.addAll(it.comments!!.map {
                            RealmComment().apply {
                                comment = it.comment
                                userId = it.userId
                                postId = it.postId
                            }
                        })
                        likes?.addAll(it.likes!!.map {
                            RealmLike().apply {
                                userId = it.userId
                                postId = it.postId
                            }
                        })
                    }
                })
            }
            realm.insertOrUpdate(userPosts)
        })

    }

    fun getALLPostsFromDb() {
        val call = service?.getAllPosts()
        call?.enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("posts", "posts : onfailure " + t.message)
            }
            override fun onResponse(
                    call: Call<List<Post>>, response: Response<List<Post>>
            ) {
                if (response.isSuccessful) {
                    savePost(response.body()!!)
                } else {

                }
            }
        })
        return
    }

    private fun savePost(posts: List<Post>) {
        realm.executeTransactionAsync(fun(realm: Realm) {
            posts.forEach { p ->

                val post = RealmPost().apply {
                    //realm = respones.body
                    uid = p.uid
                    title = p.title
                    description = p.description
                    filePath = p.filePath
                    ownerId = p.ownerId
                    comments?.addAll(p.comments!!.map {
                        RealmComment().apply {
                            comment = it.comment
                            userId = it.userId
                            postId = it.postId
                        }
                    })
                    likes?.addAll(p.likes!!.map {
                        RealmLike().apply {
                            userId = it.userId
                            postId = it.postId
                        }
                    })
                }
                realm.insertOrUpdate(post)
            }
        }, fun() {
            Log.d("realm", "all posts uploaded to realm")
        })
    }

    fun getFollowingsPostsFromDb(id: String) {

        val call = service?.getFollowingsPost(id)
        call?.enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("posts", "posts : onfailure " + t.message)
            }

            override fun onResponse(
                call: Call<List<Post>>, response: Response<List<Post>>
            ) {
                if (response.isSuccessful) {
                    saveFollowingPost(response.body()!!)
                } else {

                }
            }
        })
        return
    }

    private fun saveFollowingPost(posts: List<Post>) {

        realm.executeTransactionAsync(fun(realm: Realm) {

            var followPosts = RealmFollowingPost().apply {
                listId = 2
                followingPost?.addAll(posts.map {
                    RealmPost().apply {
                        //realm = respones.body
                        uid = it.uid
                        title = it.title
                        description = it.description
                        filePath = it.filePath
                        ownerId = it.ownerId
                        comments?.addAll(it.comments!!.map {
                            RealmComment().apply {
                                comment = it.comment
                                userId = it.userId
                                postId = it.postId
                            }
                        })
                        likes?.addAll(it.likes!!.map {
                            RealmLike().apply {
                                userId = it.userId
                                postId = it.postId
                            }
                        })
                    }
                })
            }
            realm.insertOrUpdate(followPosts)
        })

    }

    fun getAllPetsFromDB() {
        val call = service?.getAllPets()
        call?.enqueue(object : Callback<List<Pet>> {
            override fun onFailure(call: Call<List<Pet>>, t: Throwable) {
                Log.d("uus", "users : onfailure " + t.message)
            }
            override fun onResponse(
                call: Call<List<Pet>>, response: Response<List<Pet>>
            ) {
                if (response.isSuccessful) {
                    saveAllPetsToRealm(response.body()!!)
                } else {


                }
            }
        })
        return
    }

    private fun saveAllPetsToRealm(pets: List<Pet>) {
        realm.executeTransactionAsync(fun(realm: Realm) {
            pets.forEach { p ->
                val pet = RealmPet().apply {
                    //realm = respones.body
                    uid = p.uid
                    ownerId = p.ownerId
                    name = p.name
                    imageUrl = p.imageUrl
                    age = p.age
                    bio = p.bio
                    breed = p.breed
                    gender = p.gender

                    posts?.addAll(p.posts!!.map {
                        RealmPost().apply {
                            //realm = respones.body
                            uid = it.uid
                            title = it.title
                            description = it.description
                            filePath = it.filePath
                            ownerId = it.ownerId
                            comments?.addAll(it.comments!!.map {
                                RealmComment().apply {
                                    comment = it.comment
                                    userId = it.userId
                                    postId = it.postId
                                }
                            })
                            likes?.addAll(it.likes!!.map {
                                RealmLike().apply {
                                    userId = it.userId
                                    postId = it.postId
                                }
                            })
                        }
                    })

                    followers?.addAll(p.followers!!.map {
                        RealmUser().apply {
                            uid = it.uid
                            userName = it.userName
                        }
                    })
                }
                realm.insertOrUpdate(pet)
            }
        }, fun() {
            Log.d("realm", "all pets uploaded to realm")
        })
    }

    fun getPetsByOwnerFromDb(id: String) {
        val call = service?.getPetsByOwnerId(id)
        call?.enqueue(object : Callback<List<Pet>> {
            override fun onFailure(call: Call<List<Pet>>, t: Throwable) {
                Log.d("posts", "posts : onfailure " + t.message)
            }
            override fun onResponse(
                call: Call<List<Pet>>, response: Response<List<Pet>>
            ) {
                if (response.isSuccessful) {
                    savePetsByOwner(response.body()!!)
                } else {

                }
            }
        })
        return
    }

    private fun savePetsByOwner(pet: List<Pet>) {
        realm.executeTransactionAsync(fun(realm: Realm) {
            var petsbyOwner = RealmPetByOwner().apply {
                petByOwner?.addAll(pet.map {
                    RealmPet().apply {
                        //realm = respones.body
                        uid = it.uid
                        ownerId = it.ownerId
                        name = it.name
                        imageUrl = it.imageUrl
                        age = it.age
                        bio = it.bio
                        breed = it.breed
                        gender = it.gender

                        posts?.addAll(it.posts!!.map {
                            RealmPost().apply {
                                //realm = respones.body
                                uid = it.uid
                                title = it.title
                                description = it.description
                                filePath = it.filePath
                                ownerId = it.ownerId
                                comments?.addAll(it.comments!!.map {
                                    RealmComment().apply {
                                        comment = it.comment
                                        userId = it.userId
                                        postId = it.postId
                                    }
                                })
                                likes?.addAll(it.likes!!.map {
                                    RealmLike().apply {
                                        userId = it.userId
                                        postId = it.postId
                                    }
                                })
                            }
                        })

                        followers?.addAll(it.followers!!.map {
                            RealmUser().apply {
                                uid = it.uid
                                userName = it.userName
                            }
                        })
                    }
                })
            }
            realm.insertOrUpdate(petsbyOwner)
        })
    }

}
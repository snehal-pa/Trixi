package com.example.trixi.ui.register

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.trixi.R
import com.example.trixi.entities.User
import com.example.trixi.repository.PostToDb
import com.example.trixi.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File


class RegisterActivity : AppCompatActivity() {

    val post = PostToDb()
    var selectedPhotouri: Uri? = null
    lateinit var bitmap : Bitmap
    var byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    var encodedImage: String = ""
    var filePath = ""


    private val mMediaUri: Uri? = null

    private var fileUri: Uri? = null

    private var mediaPath: String? = null

    private var mImageFileLocation = ""
    private lateinit var pDialog: ProgressDialog
    private var postPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_already_account.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        register_profile_image.setOnClickListener {
            requestPermissions()
            val intent = Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            /* intent.type = "image/*"
             intent.setAction(Intent.ACTION_GET_CONTENT)*/
            */

            startActivityForResult(intent, 0)
        }

        button_register.setOnClickListener {
            saveProfileImage()
        }
    }

    private fun hasWriteExternalStoragePermission() =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if(!hasWriteExternalStoragePermission()){
            permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.d("permissionRequest", "${permissions[i]} granted.")
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //check what the selected image is

            /*selectedPhotouri = data.data

            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotouri)
            register_profile_image.setImageBitmap(bitmap)*/

            //---------------------------------------------------------------

            var selectedImage = data.getData()
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            assert(cursor != null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            mediaPath = cursor.getString(columnIndex)
            // Set the Image in ImageView for Previewing the Media

            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
            register_profile_image.setImageBitmap(bitmap)




            cursor.close()


            postPath = mediaPath



            println("PATH: " + postPath)

            val bm = BitmapFactory.decodeFile(postPath)
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG,100,   baos) // bm is the bitmap object

            val b = baos.toByteArray()

            encodedImage = Base64.encodeToString(b, Base64.DEFAULT)

            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

            //val imageInByte: ByteArray = byteArrayOutputStream.toByteArray()
            //encodedImage = Base64.encodeToString(imageInByte, Base64.DEFAULT)



            val file = File (encodedImage)
            val filename = postPath.toString()

            Log.d("Image", "Path :  $postPath ")
            Log.d("Image", "encode :  $encodedImage ")
            Log.d("Image", "file :  $file ")


            val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
            //val body = MultipartBody.Part.createFormData("pic", file.name, reqFile)
            //val name = RequestBody.create(MediaType.parse("multipart/form-data"), "upload_test")

            //Log.d("Image", "Image :  $filename ,  $reqFile , $body , $name")

            post.PostImageToServer(file)
        }
    }


    private fun saveProfileImage(){
        if (selectedPhotouri == null){
            Toast.makeText(this, "Please select profile image", Toast.LENGTH_LONG).show()
            return
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        val imageInByte: ByteArray = byteArrayOutputStream.toByteArray()
        encodedImage = Base64.encodeToString(imageInByte, Base64.DEFAULT)


        registerUser()
    }

    private fun registerUser(){
        val userName = register_username.text.toString()
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username/email/password", Toast.LENGTH_LONG).show()
            return
        }

        val user = User("", userName, email, password, "", "", "user", null, null)
        println("here")
        post.PostRegisterUserToDb(user)


    }
}
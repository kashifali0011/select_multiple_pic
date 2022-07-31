package com.exampss.selectmultiplepicproject

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.exampss.selectmultiplepicproject.databinding.ActivityMainBinding
import com.sangcomz.fishbun.BaseActivity
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter

class MainActivity : ActivityBase(), View.OnClickListener , ImageListAdapter.OnClickListener {
    lateinit var binding: ActivityMainBinding
    var imageListAdapter: ImageListAdapter? = null
    lateinit var imageList:ArrayList <Uri>
    private val REQUEST_CODE = 1
    lateinit var activity: AppCompatActivity
    var fileUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        imageList = ArrayList()
        setListener()
        setImageList()
    }

    private fun setListener(){
        binding.ivSelectPic.setOnClickListener(this)
        binding.ivCamera.setOnClickListener(this)
    }
    private fun setImageList(){
        val categoryLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.rvListOfPic.layoutManager = categoryLayoutManager
        imageListAdapter = ImageListAdapter(applicationContext, imageList, this)
        binding.rvListOfPic.adapter = imageListAdapter
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.ivSelectPic -> {
                if (imageList.size <=  4) {
                    FishBun.with(MainActivity@ this)
                        .setImageAdapter(GlideAdapter())
                        .setMaxCount(5 - imageList.size)
                        .setActionBarColor(
                            Color.parseColor("#E2F3FA"),
                            Color.parseColor("#E2F3FA"),
                            true
                        )
                        .setActionBarTitleColor(Color.parseColor("#474747"))
                        .isStartInAllView(false)
                        .startAlbum()
                }else{
                    Toast.makeText(applicationContext , "Limit is full",Toast.LENGTH_LONG).show()
                }
            }
            R.id.ivCamera ->{
                ActivityBase.activity.startCamera(REQUEST_CODE)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            FishBun.FISHBUN_REQUEST_CODE ->{
                if (resultCode == RESULT_OK) {
                    var path = data!!.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH)
                    imageList.addAll(path!!)
                    setImageList()
                }
            }
            REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    ActivityBase.activity.processCapturedPhoto(object : ICallBackUri {
                        override fun imageUri(imagePath: Uri?) {
                            imageList.add(imagePath!!)
                            imageListAdapter!!.notifyDataSetChanged()
                        }
                    })
                }
            }
        }
    }

    override fun onClick(position: Int) {
       imageList.removeAt(position)
        setImageList()
    }
}
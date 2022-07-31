package com.exampss.selectmultiplepicproject

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exampss.selectmultiplepicproject.databinding.AdapterImageListBinding

class ImageListAdapter(var context: Context , var arrayList: ArrayList<Uri>,var mClick: OnClickListener ):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       val binding: AdapterImageListBinding = AdapterImageListBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.adapter_image_list,parent, false))
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var view = holder as ViewHolder
        Glide.with(context).asBitmap().load(arrayList[position]).into(view.binding.ivSelectImg)
        view.binding.cvDeletePic.setOnClickListener {
            mClick.onClick(position)
        }
    }
    override fun getItemCount(): Int {
       return  arrayList.size
    }
    class ViewHolder(var binding: AdapterImageListBinding):
            RecyclerView.ViewHolder(binding.root)

    interface OnClickListener{
        fun onClick(position: Int)
    }
}
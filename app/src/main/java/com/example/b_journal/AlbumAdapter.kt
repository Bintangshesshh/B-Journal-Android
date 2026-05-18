package com.example.b_journal

import com.example.b_journal.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlbumAdapter(private var listAlbum: List<Album>) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_item_title)
        val tvDesc: TextView = view.findViewById(R.id.tv_item_desc)
        val tvDate: TextView = view.findViewById(R.id.tv_item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = listAlbum[position]
        // Set data ke komponen layout item_album
        holder.tvTitle.text = album.namaAlbum.uppercase()
        holder.tvDesc.text = album.deskripsi
        holder.tvDate.text = "// POSTED_AT: " + album.tanggalDibuat
    }

    override fun getItemCount(): Int = listAlbum.size

    fun masukkanDataBaru(dataBaru: List<Album>) {
        listAlbum = dataBaru
        notifyDataSetChanged()
    }
}
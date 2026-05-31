package com.example.b_journal
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AlbumAdapter(private var listAlbum: ArrayList<Album>) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_item_desc)
        val tvDate: TextView = itemView.findViewById(R.id.tv_item_date)
        val ivCover: ImageView = itemView.findViewById(R.id.iv_item_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = listAlbum[position]
        holder.tvTitle.text = album.namaAlbum.uppercase()
        holder.tvDesc.text = album.deskripsi
        holder.tvDate.text = "// POSTED_AT: ${album.tanggalDibuat}"

        if (album.urlGambar.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(album.urlGambar)
                .timeout(60000)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailAlbumActivity::class.java).apply {
                putExtra("ALBUM_ID", album.id)
                putExtra("ALBUM_NAME", album.namaAlbum)
                putExtra("ALBUM_DESC", album.deskripsi)
                putExtra("ALBUM_OWNER_ID", album.userId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listAlbum.size
    }

    fun masukkanDataBaru(dataBaru: ArrayList<Album>) {
        this.listAlbum = dataBaru
        notifyDataSetChanged()
    }
}
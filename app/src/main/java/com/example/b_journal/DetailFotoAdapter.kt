package com.example.b_journal

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetailFotoAdapter(private var listFoto: ArrayList<Foto>) :
    RecyclerView.Adapter<DetailFotoAdapter.FotoViewHolder>() {

    class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGridFoto: ImageView = itemView as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                400
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(8, 8, 8, 8)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        }
        return FotoViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = listFoto[position]

        if (foto.lokasiFile.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(foto.lokasiFile)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivGridFoto)
        } else {
            holder.ivGridFoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LihatFotoActivity::class.java).apply {
                putExtra("URL_FOTO_ASLI", foto.lokasiFile)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listFoto.size

    fun updateData(dataBaru: ArrayList<Foto>) {
        this.listFoto = dataBaru
        notifyDataSetChanged()
    }
}
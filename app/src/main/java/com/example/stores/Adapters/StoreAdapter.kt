package com.example.stores.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.Interfaces.OnclickListener
import com.example.stores.R
import com.example.stores.Models.StoreEntity
import com.example.stores.databinding.ItemStoreBinding

class StoreAdapter (private var stores: MutableList<StoreEntity>, private var listener: OnclickListener) :
RecyclerView.Adapter<StoreAdapter.ViewHolder>(){

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // se almacema el contexto en mContext
        mContext = parent.context
        // Inflamos el layout item_store
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false) // Se le agrega el layout a la variable view
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores.get(position)

        with(holder){
            setListener(store)   // llamada a la funcion setListener y le pasamos la posicion

            binding.tvName.text = store.name   // Le pasamos el nombre de la tienda a tvName
            binding.cbFavorite.isChecked = store.favorite // le pasamos la actualizacion de favoritos a cbFavorite

            // Le pasamos la imagen de la tienda a photoUrl
            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.photo )

        }
    }

    override fun getItemCount(): Int = stores.size


    // Obtener  informacion de la base de datos
    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores
        notifyDataSetChanged()

    }

    // Agregar informacion a la base de datos
    fun add(storeEntity: StoreEntity) {
        if (!stores.contains(storeEntity)){
            stores.add(storeEntity)
            notifyItemInserted(stores.size-1)

        }
    }

    // Actualiza informacion de la base de datos
    fun update(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.set(index, storeEntity)
            notifyItemChanged(index)
        }

    }

    // Elimina informacion de la base de datos
    fun delete(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index)
            notifyItemRemoved(index)
        }

    }

    // Clase interna
    inner class  ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemStoreBinding.bind(view)

        fun setListener(storeEntity: StoreEntity){
            with(binding.root){
              setOnClickListener { listener.onClick(storeEntity.id) }
              setOnLongClickListener{listener.onDeleteStore(storeEntity) // Onclick para eliminar tiendas
                true
            }
            }

            binding.cbFavorite.setOnClickListener{listener.onFavoriteStore(storeEntity)} // Onclick para actualizar favoritos


        }

    }

}
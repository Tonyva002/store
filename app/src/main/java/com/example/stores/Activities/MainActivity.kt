package com.example.stores.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.*
import com.example.stores.Adapters.StoreAdapter
import com.example.stores.Interfaces.MainAux
import com.example.stores.Interfaces.OnclickListener
import com.example.stores.Models.StoreApplication
import com.example.stores.Models.StoreEntity
import com.example.stores.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity() : AppCompatActivity(), OnclickListener, MainAux {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 1-1 Llamada al metodo para lanzar el EditStoreFragment desde el FloatingActionButton
        mBinding.fab.setOnClickListener{lunchEditFragment()}

        // 1-2 Llamada al metodo para configurar la RecyclerView
        setupRecyclerView()
    }

    // 1-1 Metodo para lanzar el EditStoreFragment
    private fun lunchEditFragment(args: Bundle? = null) {
        val fragment = EditStoreFragment()
        if (args != null) fragment.arguments = args

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.add(R.id.containerMain, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        hideFab() // Llamada al metodo donde se esconde el FloatingActionButton

    }
    // 1-2 Metodo para configurar la RecyclerView
    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)  // Se le pasan los parametros al StoreAdapter
        mGridLayout = GridLayoutManager(this, resources.getInteger(R.integer.main_columns)) // Se agrega el tipo de layout que utilizaremos

        getStore() // 1-3 Llamada al metodo para obtener la informacion de la base de datos

        // Se le agrega el layout y el adapter a la recyclerView
        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }

    }
    // 1-3 Metodo para obtener la informacion de la base de datos
    private fun getStore(){
        doAsync {
            val stores = StoreApplication.database.storeDao().getAllStores()
            uiThread {
                mAdapter.setStores(stores)
            }
        }
    }


     /*
      * OnclickLister
      */
    // 1-4 Metodo para lanzar el fragment al hacer click sobre una tienda para editarla
    override fun onClick(storeId: Long) {
         val args = Bundle()
         args.putLong(getString(R.string.arg_id), storeId)

         lunchEditFragment(args) // 1-1 Llamada al metodo para lanzar el fragment

    }

    // 1-5 Metodo para actualizar favoritos
    override fun onFavoriteStore(storeEntity: StoreEntity) {
        storeEntity.favorite = !storeEntity.favorite
        doAsync {
            StoreApplication.database.storeDao().updateStore(storeEntity)
            uiThread {
                updateStore(storeEntity) // 1-9 Llamada al metodo actualizar
            }
        }

    }

    // 1-6 Metodo para eliminar una tienda, muestra un dialogo de varias opciones antes de hacerlo
    override fun onDeleteStore(storeEntity: StoreEntity) {
     val items = resources.getStringArray(R.array.array_options_item)

        MaterialAlertDialogBuilder(this) //Dialogo que muestra varias opciones
            .setTitle(R.string.dialog_options_title)
            .setItems(items, { dialog, which -> // Se le pasa el arreglo con las opciones
                when(which){
                    0 -> confirmDelete(storeEntity) // 1-7 Llamada al metodo para eliminar una tienda

                    1 -> dial(storeEntity.phone) // 1-8 Llamada al metodo para permitir llamadar con el telefono de la tienda

                    2 -> goToWebsite(storeEntity.website) // 1.9 Llamada al metodo para ir a la pagina web
                }

            })
            .show()

    }


    // 1-7 Metodo para eliminar una tienda
    private fun confirmDelete(storeEntity: StoreEntity){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm, { dialog, which ->
                doAsync {
                    StoreApplication.database.storeDao().deleteStore(storeEntity)
                    uiThread {
                        mAdapter.delete(storeEntity)
                    }
                }
            })
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    // 1-8 Metodo para llamar con el telefono de la tienda
    private fun dial(phone: String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel:$phone")
        }
       startIntent(callIntent) // 1-10 Llamada al metodo para validar el acceso a las actividades interna del telefono (llamada telefono)

    }
    // 1-9 Metodo para ir a la pagina web
    private fun goToWebsite(website: String){
        if (website.isEmpty()){
            Toast.makeText(this, R.string.main_error_message_website, Toast.LENGTH_LONG).show()
        }else {
            val websiteIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(website)
            }
            startIntent(websiteIntent)  // 1-10 Llamada al metodo para validar el acceso a las actividades interna del telefono (website)

        }

    }
   // 1-10 Metodo para validar el acceso a las actividades interna del telefono
    private fun startIntent(intent: Intent){
        if (intent.resolveActivity(packageManager) != null)
            startActivity(intent)
        else
            Toast.makeText(this, R.string.main_error_message_resolve, Toast.LENGTH_LONG).show()

    }

    /*
    * MainAux
     */
    // 1-11 Metodo para esconder el FloatingActionButton
    override fun hideFab(isVisible: Boolean) {
        if (isVisible) mBinding.fab.show() else mBinding.fab.hide()

    }
    // 1-12 Metodo para agregar una tienda
    override fun addStore(storeEntity: StoreEntity) {
     mAdapter.add(storeEntity)
    }
    // 1-13 Metodo para actualizar una tienda
    override fun updateStore(storeEntity: StoreEntity) {
    mAdapter.update(storeEntity)
    }
}
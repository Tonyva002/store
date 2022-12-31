package com.example.stores.Activities

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.R
import com.example.stores.Models.StoreApplication
import com.example.stores.Models.StoreEntity
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if (id != null && id != 0L){
           mIsEditMode = true
           getStore(id) // 2-1 Llamada al metodo donde seleciona la informacion de la tienda por id desde la base de datos

        }else{
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "" )
        }

        setupActionBar()  // 2-3 Llamada al metodo donde se le asigna componentes a la barra de acciones
        setupTextFields() // 2-4 Llamada al metodo para actualizar el estado de los campos en tiempo real

    }


    // 2-1 Metodo para selecionar la informacion de la tienda por id desde la base de datos
    private fun getStore(id: Long) {
        doAsync {
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            uiThread {
                if (mStoreEntity != null) setUiStore(mStoreEntity!!) //1-2 Llamada al metodo donde seleciona informacion de la tienda desde la base de datos
            }
        }

    }
    //2-2 Metodo para selecionar la informacion de la tienda desde la base de datos
    private fun setUiStore(storeEntity: StoreEntity) {

        with(mBinding){
            etName.setText(storeEntity.name)
            etPhone.setText(storeEntity.phone)
            etWebsite.setText(storeEntity.website)
            etPhotoUrl.setText(storeEntity.photoUrl)


        }

    }

    // 2-3 Metodo para asignar componentes a la barra de acciones
    private fun setupActionBar() {
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = if (mIsEditMode){
            getString(R.string.edit_store_title_adit) // Le asigna un titulo de editar al fragment
        }else{
            getString(R.string.edit_store_title_add) // Le asigna un titulo de crear al fragment
        }
        setHasOptionsMenu(true)
    }

    // 2-4 Metodo para actualizar el estado de los campos en tiempo real
    private fun setupTextFields() {
        with(mBinding){
            etName.addTextChangedListener { validateFields(tilName) } // 2-8 Llamada al metodo para validar los campos
            etPhone.addTextChangedListener { validateFields(tilPhone) }
            etPhotoUrl.addTextChangedListener { validateFields(tilPhotoUrl)
                loadImage(it.toString().trim()) // 2-5 Llamada al metodo para pasar la imagen desde etPhotoUrl a imgPhoto
            }
        }
    }

    // 2-5 Metodo para pasar la imagen desde etPhotoUrl a imgPhoto
    private fun loadImage(url: String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imgPhoto)

    }


    // 2-6 Metodo para inflar el menu en la barra de acciones
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    // 2-7 Metodo para asignar funcionalidad al menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            // Almacena la informacion en la base de datos desde el fragment
            R.id.action_save -> {
                //Hacemos la validacion y llamamos el metodo para validar que los campos sean llenados
               if (mStoreEntity != null && validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone, mBinding.tilName)){

                    with(mStoreEntity!!){
                        name = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        website = mBinding.etWebsite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()

                    }
                    doAsync {
                        if (mIsEditMode){
                            StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        }else{
                            mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        }

                        uiThread {
                            hideKeyboard() // Esconde el teclado

                            if (mIsEditMode){
                                mActivity?.updateStore(mStoreEntity!!)
                                Snackbar.make(mBinding.root,
                                    R.string.edit_store_message_update_success,
                                    Snackbar.LENGTH_LONG).show() //Mensaje de tienda actualizada correctamente
                            }else{
                            mActivity?.addStore(mStoreEntity!!)
                            Toast.makeText(mActivity,
                                R.string.edit_store_message_save_success,
                                Toast.LENGTH_LONG).show() //Mensaje de tienda agregada correctamente
                            mActivity?.onBackPressed()

                            }

                        }
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
    // 2-8 Metodo con parametros para validar que los campos sean llenados
    private fun validateFields(vararg textFields: TextInputLayout): Boolean{
        var isValid = true

        for (textFild in textFields){
            if (textFild.editText?.text.toString().trim().isEmpty()){
                textFild.error = getString(R.string.helper_required)
                textFild.editText?.requestFocus()
                isValid = false
            }else{
                textFild.error = null
            }

        }
        if (!isValid)
            Toast.makeText(mActivity, R.string.edit_store_message_valid, Toast.LENGTH_LONG).show()

        return isValid
    }

    //2-9 Metodo sin parametros para validar que los campos sean llenados (Opcional)
    private fun validateFields(): Boolean {
        var isValid = true

        if (mBinding.etPhotoUrl.text.toString().trim().isEmpty()){
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            mBinding.etPhotoUrl.requestFocus()
            isValid = false

        }
        if (mBinding.etPhone.text.toString().trim().isEmpty()){
            mBinding.tilPhone.error = getString(R.string.helper_required)
            mBinding.etPhone.requestFocus()
            isValid = false

        }
        if (mBinding.etName.text.toString().trim().isEmpty()){
            mBinding.tilName.error = getString(R.string.helper_required)
            mBinding.etName.requestFocus()
            isValid = false

        }
        return isValid
    }

    //2-10 Metodo para esconder el teclado
    private fun hideKeyboard(){
        val imn = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null){
            imn.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    override fun onDestroyView() {
        hideKeyboard()   // 2-10 Llamada al metodo para esconder el teclado
        super.onDestroyView()
    }

    // 2-11 Permite visualizar el titulo de la tienda y el FloatingActionButton en la pantalla principal al darle atras desde el fragment
    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)
        setHasOptionsMenu(false)
        super.onDestroy()
    }

}
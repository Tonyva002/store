package com.example.stores.Interfaces

import com.example.stores.Models.StoreEntity

interface OnclickListener {

    fun onClick(storeId: Long)
    fun onFavoriteStore(storeEntity: StoreEntity)
    fun onDeleteStore(storeEntity: StoreEntity)

}
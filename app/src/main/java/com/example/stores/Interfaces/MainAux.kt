package com.example.stores.Interfaces

import com.example.stores.Models.StoreEntity

interface MainAux {

    fun hideFab(isVisible: Boolean = false)
    fun addStore(storeEntity: StoreEntity)
    fun updateStore(storeEntity: StoreEntity)
}
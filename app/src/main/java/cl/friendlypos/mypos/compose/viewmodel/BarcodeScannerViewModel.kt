package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BarcodeScannerViewModel : ViewModel() {
    private val _scannedBarcodes = MutableStateFlow<List<String>>(emptyList())
    val scannedBarcodes: StateFlow<List<String>> = _scannedBarcodes.asStateFlow()
    
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    fun addBarcode(barcode: String) {
        val currentList = _scannedBarcodes.value.toMutableList()
        if (!currentList.contains(barcode)) {
            currentList.add(barcode)
            _scannedBarcodes.value = currentList
        }
    }
    
    fun removeBarcode(index: Int) {
        val currentList = _scannedBarcodes.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _scannedBarcodes.value = currentList
        }
    }
    
    fun clearAllBarcodes() {
        _scannedBarcodes.value = emptyList()
    }
    
    fun toggleScanning() {
        _isScanning.value = !_isScanning.value
    }
    
    fun getAllBarcodesAsText(): String {
        return _scannedBarcodes.value.joinToString("\n")
    }

//    fun addScannedBarcode(barcode: String) {
//        if (isScanning.value) {
//            addBarcode(barcode)
//            // Aquí puedes agregar la vibración si tienes acceso al contexto
//        }
//    }
//
//    fun isCurrentlyScanning(): Boolean {
//        return isScanning.value
//    }

}
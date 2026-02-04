package com.biblia.koine.data.cache

import android.content.Context
import com.biblia.koine.data.BibleBook
import com.biblia.koine.data.repository.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BibleCache - Singleton para cache en memoria de datos estáticos de la Biblia
 * Optimización YouVersion: Los libros se cargan una sola vez al iniciar la app
 * y se mantienen en RAM para acceso instantáneo.
 */
object BibleCache {
    private var _books: List<BibleBook>? = null
    private var _isInitialized = false
    
    /**
     * Lista de libros en memoria. Acceso instantáneo sin query a DB.
     */
    val books: List<BibleBook> 
        get() = _books ?: emptyList()
    
    val isInitialized: Boolean 
        get() = _isInitialized
    
    /**
     * Inicializa el cache cargando todos los libros de la base de datos.
     * Debe llamarse en Application.onCreate() para que esté listo antes
     * de que el usuario abra cualquier pantalla.
     */
    suspend fun initialize(context: Context) {
        if (_isInitialized) return
        
        withContext(Dispatchers.IO) {
            try {
                val repository = BibleRepository(context)
                _books = repository.getBooks("RVR1960")
                _isInitialized = true
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
                _books = emptyList()
            }
        }
    }
    
    /**
     * Obtiene un libro por ID sin hacer query a la base de datos.
     * O(n) pero n es pequeño (66 libros).
     */
    fun getBook(id: String): BibleBook? {
        return _books?.find { it.id.equals(id, ignoreCase = true) }
    }
    
    /**
     * Obtiene un libro por número (1-66).
     */
    fun getBookByNumber(bookNumber: Int): BibleBook? {
        return _books?.find { it.bookNumber == bookNumber }
    }
    
    /**
     * Libros del Antiguo Testamento (1-39).
     */
    fun getOldTestamentBooks(): List<BibleBook> {
        return _books?.filter { it.bookNumber <= 39 } ?: emptyList()
    }
    
    /**
     * Libros del Nuevo Testamento (40-66).
     */
    fun getNewTestamentBooks(): List<BibleBook> {
        return _books?.filter { it.bookNumber >= 40 } ?: emptyList()
    }
    
    /**
     * Limpia el cache (útil para testing).
     */
    fun clear() {
        _books = null
        _isInitialized = false
    }
}

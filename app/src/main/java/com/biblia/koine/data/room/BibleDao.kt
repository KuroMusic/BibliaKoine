package com.biblia.koine.data.room

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.db.SupportSQLiteQuery

data class SearchResult(
    @ColumnInfo(name = "contenido") val contenido: String?,
    @ColumnInfo(name = "libro_id") val libro_id: String?,
    @ColumnInfo(name = "capitulo") val capitulo: String?,
    @ColumnInfo(name = "versiculo") val versiculo: String?,
    @ColumnInfo(name = "id") val id: Int? = null  // SQLite 'rowid'
)

@Dao
interface BibleDao {
    @SkipQueryVerification
    @Query("""
        SELECT b.rowid as id, b.contenido, b.libro_id, b.capitulo, b.versiculo 
        FROM buscador_biblico b
        INNER JOIN bible_content c ON c.id = b.rowid
        WHERE b.contenido MATCH :query 
        AND c.version = :version
        ORDER BY b.libro_id, b.capitulo, b.versiculo
    """)
    suspend fun searchBibleFTS(query: String, version: String): List<SearchResult>
    @Query("""
        SELECT * FROM bible_content 
        WHERE book_num=:book AND chapter=:chapter AND version=:version
        ORDER BY verse
    """)
    fun getChapterVerses(book: Int, chapter: Int, version: String): Flow<List<BibleVerse>>
    
    @Query("""
        SELECT * FROM bible_content 
        WHERE book_num=:book AND chapter=:chapter AND version=:version
        ORDER BY verse
    """)
    suspend fun getChapterVersesSync(book: Int, chapter: Int, version: String): List<BibleVerse>

    @Query("""
        SELECT * FROM pericopas 
        WHERE libro_id=:book 
        AND capitulo_inicio=:chapter 
        ORDER BY versiculo_inicio
    """)
    fun getPericopesForChapter(book: Int, chapter: Int): Flow<List<Pericope>>
    
    @Query("""
        SELECT * FROM pericopas 
        WHERE libro_id=:book 
        AND capitulo_inicio=:chapter 
        ORDER BY versiculo_inicio
    """)
    suspend fun getPericopesForChapterSync(book: Int, chapter: Int): List<Pericope>

    @Query("SELECT * FROM strong_concord WHERE topic=:strongNum LIMIT 1")
    fun getStrongDefinition(strongNum: String): Flow<StrongDefinition?>

    // Search support
    // Search support
    @Query("""
        SELECT * FROM bible_content 
        WHERE version=:version 
        AND text LIKE '%' || :query || '%'
        ORDER BY book_num, chapter, verse LIMIT 50
    """)
    suspend fun searchBible(query: String, version: String): List<BibleVerse>
    
    // Higher priority search: Exact topic match first, then alphabetically
    @Query("""
        SELECT * FROM strong_concord 
        WHERE topic LIKE :query || '%' 
        OR topic LIKE '%' || :query || '%'
        ORDER BY 
          CASE 
            WHEN topic = :query THEN 0
            WHEN topic LIKE :query || '%' THEN 1
            ELSE 2 
          END,
          topic ASC
        LIMIT 100
    """)
    suspend fun searchDictionary(query: String): List<StrongDefinition>

    @Query("SELECT DISTINCT book_num FROM bible_content WHERE version = :version ORDER BY id ASC")
    fun getBooks(version: String): List<Int>
    
    @Query("SELECT DISTINCT verse FROM bible_content WHERE book_num = :book_num AND chapter = :chapter AND version = :version ORDER BY verse ASC")
    suspend fun getVersesCount(book_num: Int, chapter: Int, version: String): List<Int>

    @Query("SELECT DISTINCT chapter FROM bible_content WHERE book_num = :book_num AND version = :version ORDER BY chapter ASC")
    suspend fun getChapters(book_num: Int, version: String): List<Int>
    @Query("SELECT DISTINCT codigo FROM versiones ORDER BY id ASC")
    suspend fun getAvailableVersions(): List<String>

    @Query("SELECT * FROM bible_content ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(): BibleVerse?
}

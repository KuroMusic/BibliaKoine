package com.biblia.koine.utils

import android.content.Context
import android.util.Log
import java.io.File

object AutoInstaller {
    fun installSwordModules(context: Context) {
        val swordDir = File(context.getExternalFilesDir(null), ".sword")
        val marker = File(swordDir, "sparv1960.installed")
        
        if (marker.exists()) {
            Log.d("AutoInstaller", "✅ SPARV1960 ya instalada")
            return
        }
        
        Log.d("AutoInstaller", "🚀 Instalando SPARV1960...")
        
        // Copiar mods.d/sparv1960.conf
        val modsDir = File(swordDir, "mods.d")
        if (!modsDir.exists()) modsDir.mkdirs()
        
        try {
            copyAsset(context, "mods.d/sparv1960.conf", File(modsDir, "sparv1960.conf"))
            
            // Copiar modules/texts/ztext/sparv1960/*
            val targetModuleDir = File(swordDir, "modules/texts/ztext/sparv1960")
            if (!targetModuleDir.exists()) targetModuleDir.mkdirs()
            
            copyAssetFolder(context, "modules/texts/ztext/sparv1960", targetModuleDir)
            
            marker.createNewFile()
            Log.d("AutoInstaller", "✅ SPARV1960 instalada!")
        } catch (e: Exception) {
            Log.e("AutoInstaller", "❌ Error instalando SPARV1960", e)
        }
    }
    
    private fun copyAsset(context: Context, assetPath: String, target: File) {
        try {
            context.assets.open(assetPath).use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("AutoInstaller", "📄 Copiado ${target.name}")
        } catch (e: Exception) {
            Log.e("AutoInstaller", "❌ Error copiando asset: $assetPath", e)
        }
    }
    
    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        try {
            val list = context.assets.list(assetPath)
            if (list.isNullOrEmpty()) {
                Log.w("AutoInstaller", "⚠️ Carpeta vacía o no encontrada: $assetPath")
                return
            }
            list.forEach { fileName ->
                copyAsset(context, "$assetPath/$fileName", File(targetDir, fileName))
            }
        } catch (e: Exception) {
            Log.e("AutoInstaller", "❌ Error listando assets: $assetPath", e)
        }
    }
}

package com.biblia.koine.data

data class GreekWord(
    val greek: String,
    val transliteration: String,
    val definition: String
)

object GreekWordRepository {
    private val words = listOf(
        GreekWord("ἀγάπη", "agápē", "Amor divino, amor sacrificial"),
        GreekWord("πίστις", "pístis", "Fe, confianza, creencia"),
        GreekWord("χάρις", "cháris", "Gracia, favor inmerecido"),
        GreekWord("εἰρήνη", "eirḗnē", "Paz, tranquilidad"),
        GreekWord("σοφία", "sophía", "Sabiduría, conocimiento profundo"),
        GreekWord("ἀλήθεια", "alḗtheia", "Verdad, realidad"),
        GreekWord("δόξα", "dóxa", "Gloria, esplendor"),
        GreekWord("ζωή", "zōḗ", "Vida, vida eterna"),
        GreekWord("λόγος", "lógos", "Palabra, razón, mensaje"),
        GreekWord("πνεῦμα", "pneûma", "Espíritu, aliento"),
        GreekWord("καρδία", "kardía", "Corazón, centro del ser"),
        GreekWord("ἐλπίς", "elpís", "Esperanza, expectativa"),
        GreekWord("δύναμις", "dýnamis", "Poder, fuerza, milagro"),
        GreekWord("βασιλεία", "basileía", "Reino, reinado"),
        GreekWord("ἐκκλησία", "ekklēsía", "Iglesia, asamblea"),
        GreekWord("μετάνοια", "metánoia", "Arrepentimiento, cambio de mente"),
        GreekWord("εὐαγγέλιον", "euangélion", "Evangelio, buenas nuevas"),
        GreekWord("ἁμαρτία", "hamartía", "Pecado, errar el blanco"),
        GreekWord("σωτηρία", "sōtēría", "Salvación, liberación"),
        GreekWord("δικαιοσύνη", "dikaiosýnē", "Justicia, rectitud"),
        GreekWord("ἀγαθός", "agathós", "Bueno, beneficioso"),
        GreekWord("ἅγιος", "hágios", "Santo, consagrado"),
        GreekWord("κύριος", "kýrios", "Señor, amo, dueño"),
        GreekWord("θεός", "theós", "Dios"),
        GreekWord("Χριστός", "Christós", "Cristo, ungido"),
        GreekWord("προσευχή", "proseuchḗ", "Oración, súplica"),
        GreekWord("ὑπομονή", "hypomonḗ", "Paciencia, perseverancia"),
        GreekWord("ἔλεος", "éleos", "Misericordia, compasión"),
        GreekWord("μαρτυρία", "martyría", "Testimonio, evidencia"),
        GreekWord("κοινωνία", "koinōnía", "Comunión, compañerismo")
    )
    
    fun getWordOfDay(): GreekWord {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return words[dayOfYear % words.size]
    }
}

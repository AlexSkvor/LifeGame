package org.example.ui.common

import javafx.scene.control.TextFormatter
import javafx.scene.control.TextInputDialog
import org.example.life.Mineral
import java.util.function.UnaryOperator

data class UiMineral(
    val name: String,
    val id: Int,
    val number: Int
) {
    override fun toString(): String = "$name $number"
}

fun Map<Int, Int>.toUiMineralsList(minerals: List<Mineral>): List<UiMineral> = toList()
    .map { UiMineral(minerals[it.first].name, it.first, it.second) }.sortedBy { it.id }

fun dialogChangeMineralValue(uiMineral: UiMineral, hint: String, onChange: (Int) -> Unit) {
    val dialog = TextInputDialog(uiMineral.number.toString())
    dialog.title = "Изменение ресурса"
    dialog.headerText = hint
    dialog.contentText = "Количество ресурса"
    dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
        if (change.text.matches("^[0-9]*\$".toRegex())) change
        else null
    })
    dialog.showAndWait().ifPresent { str ->
        str.toIntOrNull()?.let { onChange(it) }
    }
}
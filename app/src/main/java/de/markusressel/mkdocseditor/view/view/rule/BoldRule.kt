package de.markusressel.mkdocseditor.view.view.rule

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

class BoldRule : HighlighterRuleBase() {

    override val styles = setOf<() -> CharacterStyle>({ ForegroundColorSpan(COLOR) }, { StyleSpan(Typeface.BOLD) })

    override fun findMatches(editable: Editable): Sequence<MatchResult> {
        return PATTERN
                .findAll(editable)
    }

    companion object {
        val PATTERN = "\\*{2}(.+?)\\*{2}"
                .toRegex()
        val COLOR = Color
                .parseColor("#0091EA")
    }

}
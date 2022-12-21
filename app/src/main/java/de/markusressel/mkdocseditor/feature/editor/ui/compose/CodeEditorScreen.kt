package de.markusressel.mkdocseditor.feature.editor.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.markusressel.kodehighlighter.core.ui.KodeTextField
import de.markusressel.kodehighlighter.core.ui.KodeTextFieldDefaults
import de.markusressel.kodehighlighter.language.markdown.MarkdownRuleBook
import de.markusressel.kodehighlighter.language.markdown.colorscheme.DarkBackgroundColorSchemeWithSpanStyle
import de.markusressel.mkdocseditor.feature.editor.ui.CodeEditorViewModel
import de.markusressel.mkdocseditor.ui.activity.UiState


@Composable
internal fun CodeEditorScreen(
    uiState: UiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    codeEditorViewModel: CodeEditorViewModel = hiltViewModel(),
) {
    BackHandler(
        enabled = true,
        onBack = onBack,
    )

    val editorUiState by codeEditorViewModel.uiState.collectAsState()

    var tfv: TextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                annotatedString = editorUiState.text ?: AnnotatedString(""),
                selection = editorUiState.selection ?: TextRange.Zero,
            )
        )
    }

    remember(editorUiState.text, editorUiState.selection) {
        tfv = tfv.copy(
            annotatedString = editorUiState.text ?: AnnotatedString(""),
            selection = editorUiState.selection ?: TextRange.Zero,
        )
        derivedStateOf { true }
    }

    Column(
        modifier = modifier,
    ) {
        CodeEditorLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            text = tfv,
            onTextChanged = {
                codeEditorViewModel.onUserTextInput(it.annotatedString, it.selection)
                tfv = it
            },
            // readOnly = editorUiState.editModeActive.not()
        )

        AnimatedVisibility(
            visible = uiState.snackbar != null,
            enter = slideInVertically(),
            exit = slideOutVertically(),
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(4.dp),
                action = {
                    TextButton(onClick = {
                        // onUiEvent(UiEvent.SnackbarActionTriggered)
                    }) {
                        Text(text = uiState.snackbar?.action ?: "")
                    }
                }
            ) {
                Text(text = uiState.snackbar?.text ?: "")
            }
        }
    }
}

@Composable
private fun CodeEditorLayout(
    text: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    KodeTextField(
        modifier = modifier,
        value = text,
        languageRuleBook = MarkdownRuleBook(),
        colorScheme = DarkBackgroundColorSchemeWithSpanStyle(),
        onValueChange = onTextChanged,
        colors = KodeTextFieldDefaults.textFieldColors(
            textColor = MaterialTheme.colorScheme.onBackground
        ),
    )
}

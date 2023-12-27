package de.markusressel.mkdocseditor.feature.browser.ui.compose

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import de.markusressel.mkdocseditor.feature.browser.ui.DialogState
import de.markusressel.mkdocseditor.feature.browser.ui.FileBrowserEvent
import de.markusressel.mkdocseditor.feature.browser.ui.FileBrowserViewModel
import de.markusressel.mkdocseditor.feature.browser.ui.UiEvent
import de.markusressel.mkdocseditor.feature.main.ui.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun FileBrowserScreen(
    onNavigationEvent: (NavigationEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FileBrowserViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(
        enabled = uiState.canGoUp,
        onBack = {
            // FIXME: cannot use a coroutine here
//            val consumed = viewModel.navigateUp()
//            if (consumed.not()) {
            onBack()
//            }
        },
    )

    // Runs only on initial composition
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FileBrowserEvent.Error -> {
                    //Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is FileBrowserEvent.OpenDocumentEditor -> {
                    onNavigationEvent(NavigationEvent.NavigateToCodeEditor(documentId = event.entity.id))
                }

                is FileBrowserEvent.CreateDocument -> {
                    // TODO:
//                        val existingSections = emptyList<String>()
//
//                        MaterialDialog(context()).show {
//                            lifecycleOwner(this@FileBrowserFragment)
//                            title(R.string.create_document)
//                            input(
//                                waitForPositiveButton = false,
//                                allowEmpty = false,
//                                hintRes = R.string.hint_new_section,
//                                inputType = InputType.TYPE_CLASS_TEXT
//                            ) { dialog, text ->
//
//                                val trimmedText = text.toString().trim()
//
//                                val inputField = dialog.getInputField()
//                                val isValid = !existingSections.contains(trimmedText)
//
//                                inputField.error = when (isValid) {
//                                    true -> null
//                                    false -> getString(R.string.error_section_already_exists)
//                                }
//                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
//                            }
//
//                            positiveButton(android.R.string.ok, click = {
//                                val documentName = getInputField().text.toString().trim()
//                                viewModel.createNewDocument(documentName)
//                            })
//                            negativeButton(android.R.string.cancel)
//                        }
                }

                is FileBrowserEvent.CreateSection -> {
                    // TODO:
                    Toast.makeText(context, "Not implemented :(", Toast.LENGTH_SHORT)
                        .show()
//                        val existingSections = emptyList<String>()
//
//                        MaterialDialog(context()).show {
//                            lifecycleOwner(this@FileBrowserFragment)
//                            title(R.string.create_section)
//                            input(
//                                waitForPositiveButton = false,
//                                allowEmpty = false,
//                                hintRes = R.string.hint_new_section,
//                                inputType = InputType.TYPE_CLASS_TEXT
//                            ) { dialog, text ->
//
//                                val trimmedText = text.toString().trim()
//
//                                val inputField = dialog.getInputField()
//                                val isValid = !existingSections.contains(trimmedText)
//
//                                inputField.error = when (isValid) {
//                                    true -> null
//                                    false -> getString(R.string.error_section_already_exists)
//                                }
//                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
//                            }
//
//                            positiveButton(android.R.string.ok, click = {
//                                val sectionName = getInputField().text.toString().trim()
//                                viewModel.createNewSection(sectionName)
//                            })
//                            negativeButton(android.R.string.cancel)
//                        }
                }

                is FileBrowserEvent.RenameDocument -> {
                    // TODO
                    Toast.makeText(context, "Not implemented :(", Toast.LENGTH_SHORT)
                        .show()
//                        val existingDocuments = emptyList<String>()
//
//                        MaterialDialog(context()).show {
//                            lifecycleOwner(this@FileBrowserFragment)
//                            title(R.string.edit_document)
//                            input(
//                                waitForPositiveButton = false,
//                                allowEmpty = false,
//                                prefill = event.entity.name,
//                                inputType = InputType.TYPE_CLASS_TEXT
//                            ) { dialog, text ->
//
//                                val trimmedText = text.toString().trim()
//
//                                val inputField = dialog.getInputField()
//                                val isValid = !existingDocuments.contains(trimmedText)
//
//                                inputField.error = when (isValid) {
//                                    true -> null
//                                    false -> getString(R.string.error_document_already_exists)
//                                }
//                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
//                            }
//
//                            positiveButton(android.R.string.ok, click = {
//                                val documentName = getInputField().text.toString().trim()
//                                viewModel.renameDocument(event.entity.id, documentName)
//                            })
//                            neutralButton(R.string.delete, click = {
//                                viewModel.deleteDocument(event.entity.id)
//                            })
//                            negativeButton(android.R.string.cancel)
//                        }
                }
            }
        }
    }

    FileBrowserScreenContent(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent
    )

    when (val dialogState = uiState.currentDialogState) {
        is DialogState.CreateDocument -> {
            CreateDocumentDialog(
                uiState = dialogState,
                onSaveClicked = { text ->
                    viewModel.onUiEvent(
                        UiEvent.CreateDocumentDialogSaveClicked(
                            dialogState.sectionId,
                            text
                        )
                    )
                },
                onDismissRequest = {
                    viewModel.onUiEvent(UiEvent.DismissDialog)
                },
            )
        }

        is DialogState.CreateSection -> {
            CreateSectionDialog(
                uiState = dialogState,
                onSaveClicked = { text ->
                    viewModel.onUiEvent(
                        UiEvent.CreateSectionDialogSaveClicked(
                            dialogState.parentSectionId,
                            text
                        )
                    )
                },
                onDismissRequest = {
                    viewModel.onUiEvent(UiEvent.DismissDialog)
                },
            )
        }

        else -> {}
    }
}


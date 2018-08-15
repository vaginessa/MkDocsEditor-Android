package de.markusressel.mkdocseditor.view.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.*
import android.widget.Toast
import androidx.core.widget.toast
import com.github.ajalt.timberkt.Timber
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.trello.rxlifecycle2.android.lifecycle.kotlin.bindToLifecycle
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import de.markusressel.mkdocseditor.R
import de.markusressel.mkdocseditor.extensions.prettyPrint
import de.markusressel.mkdocseditor.extensions.runOnUiThread
import de.markusressel.mkdocseditor.view.component.LoadingComponent
import de.markusressel.mkdocseditor.view.component.OptionsMenuComponent
import de.markusressel.mkdocseditor.view.fragment.base.DaggerSupportFragmentBase
import de.markusressel.mkdocseditor.view.fragment.preferences.KutePreferencesHolder
import de.markusressel.mkdocseditor.view.view.CodeEditorView
import de.markusressel.mkdocsrestclient.BasicAuthConfig
import de.markusressel.mkdocsrestclient.websocket.DocumentSyncManager
import de.markusressel.mkdocsrestclient.websocket.EditRequestEntity
import de.markusressel.mkdocsrestclient.websocket.diff.diff_match_patch
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Server Status fragment
 *
 * Created by Markus on 07.01.2018.
 */
class CodeEditorFragment : DaggerSupportFragmentBase() {

    override val layoutRes: Int
        get() = R.layout.fragment_editor

    @Inject
    lateinit var preferencesHolder: KutePreferencesHolder

    private lateinit var codeEditorView: CodeEditorView

    val documentId by lazy {
        arguments?.getString(KEY_ID)!!
    }

    private var currentText: String by savedInstanceState("")

    private var currentXPosition by savedInstanceState(0F)
    private var currentYPosition by savedInstanceState(0F)
    private var currentZoom: Float? by savedInstanceState()

    private var initialTextLoaded = false

    private lateinit var syncManager: DocumentSyncManager

    private var previouslySentPatches: MutableMap<String, String> = mutableMapOf()

    @Inject
    lateinit var diffMatchPatch: diff_match_patch

    private val loadingComponent by lazy { LoadingComponent(this) }

    private val optionsMenuComponent: OptionsMenuComponent by lazy {
        OptionsMenuComponent(this, optionsMenuRes = R.menu.options_menu_editor, onCreateOptionsMenu = { menu: Menu?, menuInflater: MenuInflater? ->
            // set refresh icon
            val refreshIcon = iconHandler
                    .getOptionsMenuIcon(MaterialDesignIconic.Icon.gmi_refresh)
            menu
                    ?.findItem(R.id.refresh)
                    ?.icon = refreshIcon
        }, onOptionsMenuItemClicked = {
            when {
                it.itemId == R.id.refresh -> {
                    loadingComponent
                            .showLoading()
                    if (syncManager.isConnected()) {
                        syncManager
                                .disconnect(1100, "Editor want's to refresh connection")
                        syncManager
                                .connect()
                    } else {
                        syncManager
                                .connect()
                    }

                    true
                }
                else -> false
            }
        })
    }

    override fun initComponents(context: Context) {
        super
                .initComponents(context)
        loadingComponent
        optionsMenuComponent
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super
                .onCreateOptionsMenu(menu, inflater)
        optionsMenuComponent
                .onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (super.onOptionsItemSelected(item)) {
            return true
        }
        return optionsMenuComponent
                .onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super
                .onCreate(savedInstanceState)

        val host = preferencesHolder
                .connectionUriPreference
                .persistedValue

        syncManager = DocumentSyncManager(documentId = documentId, url = "ws://$host/document/$documentId/ws", basicAuthConfig = BasicAuthConfig(preferencesHolder.basicAuthUserPreference.persistedValue, preferencesHolder.basicAuthPasswordPreference.persistedValue), onInitialText = {
            // TODO: there has to be a better way to do this...
            Thread
                    .sleep(1000)

            runOnUiThread {
                activity!!
                        .toast("Connected", Toast.LENGTH_SHORT)

                loadingComponent
                        .showContent()

                currentText = it
                codeEditorView
                        .setText(it)

                codeEditorView
                        .setEditable(true)

                val zoom = currentZoom ?: 1F
                codeEditorView
                        .moveTo(zoom, currentXPosition, currentYPosition, false)

                initialTextLoaded = true
            }
        }, onPatchReceived = { editRequest ->
            processEditRequest(editRequest)
        }, onError = { code, throwable ->
            throwable
                    ?.let {
                        runOnUiThread {
                            context!!
                                    .toast("Disconnected", Toast.LENGTH_SHORT)

                            codeEditorView
                                    .setEditable(false)

                            //                            loadingComponent
                            //                                    .showError(it)
                        }
                        Timber
                                .e(throwable) { "Websocket error code: $code" }
                    }
        })
    }

    @Synchronized
    private fun processEditRequest(editRequest: EditRequestEntity) {
        if (documentId != this.documentId) {
            return
        }

        if (previouslySentPatches.containsKey(editRequest.requestId)) {
            previouslySentPatches
                    .remove(editRequest.requestId)
            return
        }

        runOnUiThread {
            val oldSelection = codeEditorView
                    .editTextView
                    .selectionStart

            // parse and apply patches
            val patches: LinkedList<diff_match_patch.Patch> = diffMatchPatch.patch_fromText(editRequest.patches) as LinkedList<diff_match_patch.Patch>

            currentText = diffMatchPatch.patch_apply(patches, currentText)[0] as String

            // set new cursor position
            val newSelection = calculateNewSelectionIndex(oldSelection, patches)
                    .coerceIn(0, currentText.length)


            codeEditorView
                    .setText(currentText)

            codeEditorView
                    .editTextView
                    .setSelection(newSelection)
        }
    }

    private fun calculateNewSelectionIndex(oldSelection: Int, patches: LinkedList<diff_match_patch.Patch>): Int {
        var newSelection = oldSelection

        var currentIndex: Int
        // calculate how many characters have been inserted before the cursor
        patches
                .forEach {
                    val patch = it
                    currentIndex = patch
                            .start1

                    it
                            .diffs
                            .forEach {
                                val diff = it

                                when (diff.operation) {
                                    diff_match_patch.Operation.DELETE -> {
                                        if (currentIndex < newSelection) {
                                            newSelection -= diff
                                                    .text
                                                    .length
                                        }
                                    }
                                    diff_match_patch.Operation.INSERT -> {
                                        if (currentIndex < newSelection) {
                                            newSelection += diff
                                                    .text
                                                    .length
                                        }
                                    }
                                    else -> {
                                        currentIndex += diff
                                                .text
                                                .length
                                    }
                                }
                            }
                }

        return newSelection
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
        return loadingComponent
                .onCreateView(inflater, parent, savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super
                .onViewCreated(view, savedInstanceState)

        codeEditorView = view
                .findViewById(R.id.codeEditorView)

        RxTextView
                .textChanges(codeEditorView.editTextView)
                .skipInitialValue()
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this as LifecycleOwner)
                .subscribeBy(onNext = {
                    sendPatchIfChanged(it)
                }, onError = {
                    context
                            ?.toast(it.prettyPrint(), Toast.LENGTH_LONG)
                })

        // zoom in
        codeEditorView
                .post {
                    // remember zoom and pan
                    Observable
                            .interval(500, TimeUnit.MILLISECONDS)
                            .filter {
                                initialTextLoaded
                            }
                            .bindToLifecycle(codeEditorView)
                            .subscribeBy(onNext = {
                                currentXPosition = codeEditorView
                                        .panX
                                currentYPosition = codeEditorView
                                        .panY
                                currentZoom = codeEditorView
                                        .zoom
                            })
                }
    }

    @Synchronized
    private fun sendPatchIfChanged(it: CharSequence) {
        if (currentText.contentEquals(it)) {
            Timber
                    .e { "TEXT IST GLEICH" }
            return
        }

        val newText = it
                .toString()

        // TODO: only send patch if the change is coming from user input
        val requestId = (syncManager.sendPatch(currentText, newText))
        previouslySentPatches[requestId] = "sent"
        currentText = newText
    }

    override fun onStart() {
        super
                .onStart()

        syncManager
                .connect()
    }

    override fun onStop() {
        syncManager
                .disconnect(1000, "Editor was closed")

        super
                .onStop()
    }

    companion object {

        private const val KEY_ID = "KEY_ID"

        fun newInstance(id: String): CodeEditorFragment {
            val fragment = CodeEditorFragment()
            val bundle = Bundle()
            bundle
                    .putString(KEY_ID, id)

            fragment
                    .arguments = bundle

            return fragment
        }
    }
}

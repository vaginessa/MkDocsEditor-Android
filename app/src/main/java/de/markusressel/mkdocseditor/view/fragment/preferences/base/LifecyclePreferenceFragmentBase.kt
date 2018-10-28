package de.markusressel.mkdocseditor.view.fragment.preferences.base

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.CheckResult
import android.view.View
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


/**
 * Created by Markus on 16.02.2018.
 */
abstract class LifecyclePreferenceFragmentBase : DaggerKutePreferenceFragmentBase(), LifecycleProvider<FragmentEvent> {

    private val lifecycleSubject: BehaviorSubject<FragmentEvent> = BehaviorSubject
            .create()

    @CheckResult
    override fun lifecycle(): Observable<FragmentEvent> {
        return lifecycleSubject
                .hide()
    }

    @CheckResult
    override fun <T> bindUntilEvent(event: FragmentEvent): LifecycleTransformer<T> {
        return RxLifecycle
                .bindUntilEvent(lifecycleSubject, event)
    }

    @CheckResult
    override fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return RxLifecycleAndroid
                .bindFragment(lifecycleSubject)
    }

    override fun onAttach(context: Context) {
        super
                .onAttach(context)
        lifecycleSubject
                .onNext(FragmentEvent.ATTACH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super
                .onCreate(savedInstanceState)
        lifecycleSubject
                .onNext(FragmentEvent.CREATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super
                .onViewCreated(view, savedInstanceState)
        lifecycleSubject
                .onNext(FragmentEvent.CREATE_VIEW)
    }

    @CallSuper
    override fun onStart() {
        super
                .onStart()
        lifecycleSubject
                .onNext(FragmentEvent.START)
    }

    @CallSuper
    override fun onResume() {
        super
                .onResume()
        lifecycleSubject
                .onNext(FragmentEvent.RESUME)
    }

    @CallSuper
    override fun onPause() {
        lifecycleSubject
                .onNext(FragmentEvent.PAUSE)
        super
                .onPause()
    }

    @CallSuper
    override fun onStop() {
        lifecycleSubject
                .onNext(FragmentEvent.STOP)
        super
                .onStop()
    }

    override fun onDestroyView() {
        lifecycleSubject
                .onNext(FragmentEvent.DESTROY_VIEW)
        super
                .onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        lifecycleSubject
                .onNext(FragmentEvent.DESTROY)
        super
                .onDestroy()
    }

    override fun onDetach() {
        lifecycleSubject
                .onNext(FragmentEvent.DETACH)
        super
                .onDetach()
    }

}
/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.di.component.FragmentComponent;
import de.wladimirwendland.bibleaxis.di.module.FragmentModule;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseFragment<T extends BasePresenter> extends Fragment implements BaseView {

    @Inject protected T presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(getFragmentComponent());
        attachView();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onViewCreated();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        detachView();
    }

    @Override
    public Scheduler backgroundThread() {
        return Schedulers.newThread();
    }

    @Override
    public Scheduler mainThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public void showProgress(boolean cancelable) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showToast(int resource) {
        Toast.makeText(getContext(), resource, Toast.LENGTH_SHORT).show();
    }

    protected abstract void inject(FragmentComponent component);

    protected abstract void attachView();

    protected void detachView() {
        presenter.detachView();
    }

    private FragmentComponent getFragmentComponent() {
        return BibleAxisApp.instance(getContext())
                .getAppComponent()
                .fragmentComponent(new FragmentModule());
    }
}

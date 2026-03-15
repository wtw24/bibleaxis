/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@SuppressWarnings("WeakerAccess")
public abstract class BasePresenter<T extends BaseView> {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private WeakReference<T> viewRef;

    public void attachView(T view) {
        viewRef = new WeakReference<>(view);
    }

    public void detachView() {
        viewRef.clear();
        compositeDisposable.clear();
    }

    public abstract void onViewCreated();

    protected void addSubscription(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Nullable
    protected T getView() {
        return viewRef.get();
    }

    protected void getViewAndExecute(@NonNull Command<T> command) {
        T view = getView();
        if (view != null) {
            command.execute(view);
        }
    }

    /**
     * Команда для выполнения на View
     *
     * @param <T> тип класса View
     */
    protected interface Command<T> {

        void execute(@NonNull T view);
    }
}

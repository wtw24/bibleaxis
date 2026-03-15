/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.imagepreview;

import de.wladimirwendland.bibleaxis.managers.Librarian;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Ignore("Класс устарел")
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ImagePreviewPresenterTest {

    @Mock Librarian librarian;
//    @Mock ImagePreviewView view;
//    private ImagePreviewPresenter presenter;

    @Before
    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//        presenter = new ImagePreviewPresenter(librarian);
//        presenter.attachView(view);
    }

    @Test
    public void setImagePath() throws Exception {
//        presenter.setImagePath(null);
//        presenter.setImagePath("");
    }

    @Test
    public void onViewCreatedWithNullImagePath() throws Exception {
//        presenter.setImagePath(null);
//        presenter.onViewCreated();
//        verify(view).imageNotFound();
    }

    @Test
    public void onViewCreatedWithoutBitmap() throws Exception {
//        presenter.setImagePath("");
//        when(librarian.getModuleImage(anyString())).thenReturn(null);
//        presenter.onViewCreated();
//        verify(view).imageNotFound();
    }

    @Test
    public void onViewCreatedWithBitmap() throws Exception {
//        presenter.setImagePath("");
//        when(librarian.getModuleImage(anyString())).thenReturn(mock(Bitmap.class));
//        presenter.onViewCreated();
//        verify(view, never()).imageNotFound();
//        verify(view).updatePreviewDrawable(any());
    }
}
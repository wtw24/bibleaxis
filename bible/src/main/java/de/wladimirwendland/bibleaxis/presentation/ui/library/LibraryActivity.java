/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.library;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.InputType;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.async.task.AsyncOpenModule;
import de.wladimirwendland.bibleaxis.async.task.LoadModuleFromFile;
import de.wladimirwendland.bibleaxis.async.task.SyncModulesFromTreeTask;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.ExceptionHelper;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.entity.ItemList;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.dialogs.DialogUiHelper;
import de.wladimirwendland.bibleaxis.presentation.dialogs.NotifyDialog;
import de.wladimirwendland.bibleaxis.presentation.ui.base.AsyncTaskActivity;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import de.wladimirwendland.bibleaxis.utils.Task;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.wladimirwendland.bibleaxis.data.library.LibraryContext;

public class LibraryActivity extends AsyncTaskActivity {

    private static final int ACTION_CODE_GET_FILE = 1;
    private static final int ACTION_CODE_PICK_MODULES_FOLDER = 2;
    private static final int MODULE_VIEW = 1, BOOK_VIEW = 2, CHAPTER_VIEW = 3;
    private static final String TAG = LibraryActivity.class.getSimpleName();

    @BindView(R.id.books) ListView booksList;
    @BindView(R.id.btnBook) Button btnBook;
    @BindView(R.id.btnChapter) Button btnChapter;
    @BindView(R.id.btnModule) Button btnModule;
    @BindView(R.id.btnApply) ImageButton btnApply;
    @BindView(R.id.chapterContainer) LinearLayout chapterContainer;
    @BindView(R.id.chapterChoose) GridView chapterList;
    @BindView(R.id.modules) ListView modulesList;

    @Inject
    Librarian librarian;
    @Inject
    LibraryContext mLibraryContext;
    @Inject
    ILibraryController mILibraryController;

    private String bookID = Librarian.EMPTY_OBJ;
    private ArrayList<ItemList> books = new ArrayList<>();
    private String chapter = Librarian.EMPTY_OBJ;
    private List<String> chapters = new ArrayList<>();
    private String messageRefresh;
    private String moduleID = Librarian.EMPTY_OBJ;
    private int modulePos, bookPos, chapterPos;
    private ArrayList<ItemList> modules = new ArrayList<>();
    private int viewMode = 1;
    private final Random random = new Random();
    private PreferenceHelper prefHelper;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, LibraryActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        ButterKnife.bind(this);
        modulesList.setItemsCanFocus(false);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        btnModule.setAllCaps(false);
        btnBook.setAllCaps(false);
        prefHelper = BibleAxisApp.getInstance().getPrefHelper();

        messageRefresh = getResources().getString(R.string.messageRefresh);

        BibleReference osisLink = librarian.getCurrentOSISLink();
        if (librarian.isOSISLinkValid(osisLink)) {
            moduleID = osisLink.getModuleID();
            bookID = osisLink.getBookID();
            chapter = String.valueOf(osisLink.getChapter());
            updateView(CHAPTER_VIEW);
        } else {
            updateView(MODULE_VIEW);
        }
        setButtonText();
    }

    @Override
    protected void inject(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infl = getMenuInflater();
        infl.inflate(R.menu.menu_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_bar_refresh:
                onRefreshModulesClicked();
                return true;
            case R.id.menu_library_add:
                choiceModuleFromFile();
                return true;
            case R.id.menu_library_pick_folder:
                choiceModulesFolder();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateView(viewMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_CODE_GET_FILE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                getModuleFromFile(data.getData());
            }
        } else if (requestCode == ACTION_CODE_PICK_MODULES_FOLDER) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                onModulesFolderPicked(data);
            } else {
                Toast.makeText(this, R.string.modules_storage_location_required, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Unknown request code: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTaskComplete(Task task) {
        if (task == null || task.isCancelled()) {
            return;
        }

        if (task instanceof AsyncOpenModule) {
            onOpenModuleComplete((AsyncOpenModule) task);
        } else if (task instanceof LoadModuleFromFile) {
            onLoadModuleComplete((LoadModuleFromFile) task);
        } else if (task instanceof SyncModulesFromTreeTask) {
            onSyncModulesComplete((SyncModulesFromTreeTask) task);
        } else {
            updateView(MODULE_VIEW);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @OnItemClick(R.id.books)
    void onClickBookItem(int position) {
        bookPos = position;
        bookID = books.get(bookPos).get("ID");
        chapterPos = 0;

        updateView(CHAPTER_VIEW);
        setButtonText();

        if (chapters.size() == 1) {
            chapter = chapters.get(0);
            readChapter();
        }
    }

    @OnItemClick(R.id.chapterChoose)
    void onClickChapterItem(int position) {
        chapterPos = position;
        chapter = chapters.get(position);
        setButtonText();
        readChapter();
    }

    @OnItemClick(R.id.modules)
    void onClickModuleItem(int position2) {
        modules = librarian.getModulesList();
        if (modules.size() <= position2) {
            updateView(MODULE_VIEW);
            return;
        }
        modulePos = position2;
        moduleID = modules.get(modulePos).get(ItemList.ID);
        bookPos = 0;
        chapterPos = 0;

        String message = getResources().getString(R.string.messageLoadBooks);
        BibleReference currentOSISLink = librarian.getCurrentOSISLink();
        BibleReference osisLink1 = new BibleReference(
                currentOSISLink.getModuleDatasource(),
                null,
                moduleID,
                currentOSISLink.getBookID(),
                currentOSISLink.getChapter(),
                currentOSISLink.getFromVerse());

        mAsyncManager.setupTask(new AsyncOpenModule(message, false, osisLink1), this);
    }

    @OnClick({R.id.btnBook, R.id.btnChapter, R.id.btnModule, R.id.btnApply})
    void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBook:
                onClickBook();
                break;
            case R.id.btnChapter:
                onClickChapter();
                break;
            case R.id.btnModule:
                onClickModule();
                break;
            case R.id.btnApply:
                readChapter();
                break;
        }
    }

    private void choiceModuleFromFile() {
        final Intent target = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("application/zip")
                .addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(target, ACTION_CODE_GET_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.exception_add_module_from_file, Toast.LENGTH_LONG).show();
        }
    }

    private SimpleAdapter getBookAdapter() {
        books = new ArrayList<>();
        if (!librarian.getModulesList().isEmpty()) {
            try {
                books = librarian.getModuleBooksList(moduleID);
            } catch (OpenModuleException e) {
                ExceptionHelper.onOpenModuleException(e, this, TAG);
            } catch (BooksDefinitionException e) {
                ExceptionHelper.onBooksDefinitionException(e, this, TAG);
            } catch (BookDefinitionException e) {
                ExceptionHelper.onBookDefinitionException(e, this, TAG);
            }
        }
        return new SimpleAdapter(this, books,
                R.layout.item_list,
                new String[]{ItemList.ID, ItemList.Name}, new int[]{
                R.id.id, R.id.name});
    }

    private ArrayAdapter<String> getChapterAdapter() {
        try {
            chapters = librarian.getChaptersList(moduleID, bookID);
        } catch (BookNotFoundException e) {
            ExceptionHelper.onBookNotFoundException(e, this, TAG);
        } catch (OpenModuleException e) {
            ExceptionHelper.onOpenModuleException(e, this, TAG);
        }
        chapterPos = chapters.indexOf(chapter);
        return new ArrayAdapter<String>(this, R.layout.chapter_item, R.id.chapter, chapters) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView chapterView = view.findViewById(R.id.chapter);
                if (position == chapterPos) {
                    chapterView.setBackgroundResource(R.drawable.bg_library_chapter_tile_selected);
                    chapterView.setTextColor(ContextCompat.getColor(LibraryActivity.this, R.color.toolbarTextColor));
                } else {
                    chapterView.setBackgroundResource(R.drawable.bg_library_chapter_tile);
                    chapterView.setTextColor(ContextCompat.getColor(LibraryActivity.this, R.color.library_picker_text));
                }
                return view;
            }
        };
    }

    private ArrayAdapter<ItemList> getModuleAdapter() {
        modules = librarian.getModulesList();
        return new ArrayAdapter<ItemList>(this, R.layout.item_module, R.id.name, modules) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ItemList module = getItem(position);
                if (module == null) {
                    return view;
                }

                TextView idView = view.findViewById(R.id.id);
                TextView nameView = view.findViewById(R.id.name);
                ImageButton deleteButton = view.findViewById(R.id.module_delete);

                idView.setText(module.get(ItemList.ID));
                nameView.setText(module.get(ItemList.Name));
                deleteButton.setOnClickListener(v -> showDeleteModuleDialog(module));

                return view;
            }
        };
    }

    private void showDeleteModuleDialog(ItemList module) {
        String code = String.format(Locale.US, "%04d", random.nextInt(10000));

        LinearLayout content = DialogUiHelper.createContainer(this);

        TextView messageView = new TextView(this);
        messageView.setText(getString(R.string.module_delete_message_intro, module.get(ItemList.ID)));

        TextView codeView = new TextView(this);
        codeView.setText(code);
        codeView.setTextColor(ContextCompat.getColor(this, R.color.black));
        codeView.setTextSize(24);
        codeView.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        codeParams.setMargins(DialogUiHelper.dp(this, 20), DialogUiHelper.dp(this, 12), DialogUiHelper.dp(this, 20), 0);
        codeView.setLayoutParams(codeParams);

        EditText codeInput = new EditText(this);
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeInput.setHint(R.string.module_delete_code_hint);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(0, DialogUiHelper.dp(this, 16), 0, 0);
        codeInput.setLayoutParams(inputParams);

        content.addView(messageView);
        content.addView(codeView);
        content.addView(codeInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_dialog_header_logo)
                .setTitle(R.string.module_delete_title)
                .setView(content)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String typedCode = codeInput.getText().toString().trim();
                    if (!code.equals(typedCode)) {
                        codeInput.setError(getString(R.string.module_delete_code_wrong));
                        return;
                    }
                    dialog.dismiss();
                    deleteModule(module.get(ItemList.ID));
                }));

        dialog.show();
    }

    private void deleteModule(String targetModuleId) {
        boolean deleteOk = false;
        try {
            BaseModule targetModule = mILibraryController.getModuleByID(targetModuleId);
            if (targetModule instanceof de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule) {
                File moduleFile = new File(((de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule) targetModule).getModulePath());
                deleteOk = deleteFileOrDir(moduleFile);
            }
        } catch (OpenModuleException e) {
            ExceptionHelper.onOpenModuleException(e, this, TAG);
        }

        mILibraryController.reloadModules();
        modules = librarian.getModulesList();
        boolean stillExists = containsModuleId(targetModuleId);

        if (!stillExists) {
            Toast.makeText(this, R.string.removed, Toast.LENGTH_SHORT).show();
            if (targetModuleId.equals(moduleID)) {
                moduleID = Librarian.EMPTY_OBJ;
                bookID = Librarian.EMPTY_OBJ;
                chapter = Librarian.EMPTY_OBJ;
            }
            setButtonText();
            updateView(MODULE_VIEW);
        } else {
            int messageId = deleteOk ? R.string.module_delete_failed_reload : R.string.file_not_moved;
            Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
        }
    }

    private boolean containsModuleId(String id) {
        for (ItemList module : modules) {
            if (id.equals(module.get(ItemList.ID))) {
                return true;
            }
        }
        return false;
    }

    private boolean deleteFileOrDir(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!deleteFileOrDir(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    private void getModuleFromFile(Uri uri) {
        mAsyncManager.setupTask(new LoadModuleFromFile(
                this,
                getString(R.string.copy_module_from_file),
                uri,
                mILibraryController,
                mLibraryContext), this);
    }

    private void onRefreshModulesClicked() {
        String treeUriText = prefHelper.getModulesSourceTreeUri();
        if (treeUriText == null || treeUriText.isEmpty()) {
            choiceModulesFolder();
            return;
        }
        startModulesSync(Uri.parse(treeUriText));
    }

    private void choiceModulesFolder() {
        Intent target = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        try {
            startActivityForResult(target, ACTION_CODE_PICK_MODULES_FOLDER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.modules_storage_picker_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void onModulesFolderPicked(Intent data) {
        Uri treeUri = data.getData();
        if (treeUri == null) {
            Toast.makeText(this, R.string.modules_storage_location_required, Toast.LENGTH_LONG).show();
            return;
        }

        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.modules_storage_access_denied, Toast.LENGTH_LONG).show();
            return;
        }

        prefHelper.setModulesSourceTreeUri(treeUri.toString());
        Toast.makeText(this, R.string.modules_storage_location_saved, Toast.LENGTH_SHORT).show();
        startModulesSync(treeUri);
    }

    private void startModulesSync(Uri treeUri) {
        mAsyncManager.setupTask(new SyncModulesFromTreeTask(
                this,
                getString(R.string.messageRefresh),
                treeUri,
                mILibraryController,
                mLibraryContext), this);
    }

    private void onClickBook() {
        if (bookID.equals(Librarian.EMPTY_OBJ)) {
            return;
        }
        updateView(BOOK_VIEW);
    }

    private void onClickChapter() {
        if (chapter.equals(Librarian.EMPTY_OBJ)) {
            return;
        }
        updateView(CHAPTER_VIEW);
    }

    private void onClickModule() {
        updateView(MODULE_VIEW);
    }

    private void onLoadModuleComplete(LoadModuleFromFile task) {
        LoadModuleFromFile.StatusCode statusCode = task.getStatusCode();
        String errorMessage;
        switch (statusCode) {
            case Success:
                updateView(MODULE_VIEW);
                return;
            case FileNotExist:
                errorMessage = getString(R.string.file_not_exist);
                break;
            case FileNotSupported:
                errorMessage = getString(R.string.file_not_supported);
                break;
            case MoveFailed:
                errorMessage = getString(R.string.file_not_moved);
                break;
            case LibraryNotFound:
                errorMessage = getString(R.string.file_not_moved);
                break;
            default:
                errorMessage = getString(R.string.err_load_module_unknown);
        }
        new NotifyDialog(errorMessage, this).show();
    }

    private void onOpenModuleComplete(AsyncOpenModule task) {
        Exception e = task.getException();
        if (e == null) {
            BaseModule module = task.getModule();
            moduleID = module.getID();
            Map<String, Book> books = module.getBooks();
            if (books != null && books.size() != 0 && !books.containsKey(bookID)) {
                Iterator<String> iterator = books.keySet().iterator();
                bookID = iterator.next();
            }
            setButtonText();
            updateView(BOOK_VIEW);
        } else {
            if (e instanceof OpenModuleException) {
                ExceptionHelper.onOpenModuleException((OpenModuleException) e, this, TAG);

            } else if (e instanceof BooksDefinitionException) {
                ExceptionHelper.onBooksDefinitionException((BooksDefinitionException) e, this, TAG);

            } else if (e instanceof BookDefinitionException) {
                ExceptionHelper.onBookDefinitionException((BookDefinitionException) e, this, TAG);
            }
            updateView(MODULE_VIEW);
        }
    }

    private void onSyncModulesComplete(SyncModulesFromTreeTask task) {
        SyncModulesFromTreeTask.StatusCode statusCode = task.getStatusCode();
        switch (statusCode) {
            case Success:
            case PartialSuccess:
                Toast.makeText(this, task.getSummaryText(), Toast.LENGTH_LONG).show();
                if (task.isAppFolderCreated() || task.isModulesFolderCreated()) {
                    Toast.makeText(this, R.string.modules_storage_structure_created, Toast.LENGTH_SHORT).show();
                }
                updateView(MODULE_VIEW);
                return;
            case StorageFolderInvalid:
                new NotifyDialog(getString(R.string.modules_storage_folder_invalid), this).show();
                return;
            case SourcePermissionDenied:
                new NotifyDialog(getString(R.string.modules_storage_access_denied), this).show();
                return;
            case AppFolderCreateFailed:
                new NotifyDialog(getString(R.string.modules_storage_structure_create_failed), this).show();
                return;
            case LibraryNotFound:
                new NotifyDialog(getString(R.string.file_not_moved), this).show();
                return;
            case CopyFailed:
            default:
                new NotifyDialog(getString(R.string.modules_sync_failed), this).show();
        }
    }

    private void readChapter() {
        setResult(RESULT_OK, new Intent()
                .putExtra("linkOSIS", String.format("%s.%s.%s", moduleID, bookID, chapter)));
        finish();
    }

    private void setButtonText() {
        String bookName = Librarian.EMPTY_OBJ;
        if (!moduleID.equals(Librarian.EMPTY_OBJ) && !bookID.equals(Librarian.EMPTY_OBJ)) {
            try {
                bookName = librarian.getBookFullName(moduleID, bookID);
                List<String> chList = librarian.getChaptersList(moduleID, bookID);
                if (!chList.isEmpty()) {
                    chapter = chList.contains(chapter) ? chapter : chList.get(0);
                } else {
                    chapter = Librarian.EMPTY_OBJ;
                }
            } catch (OpenModuleException e) {
                ExceptionHelper.onOpenModuleException(e, this, TAG);
                moduleID = Librarian.EMPTY_OBJ;
                bookID = Librarian.EMPTY_OBJ;
                chapter = Librarian.EMPTY_OBJ;
            } catch (BookNotFoundException e) {
                ExceptionHelper.onBookNotFoundException(e, this, TAG);
                bookID = Librarian.EMPTY_OBJ;
                chapter = Librarian.EMPTY_OBJ;
            }
        }

        btnModule.setText(getModuleButtonText());
        btnBook.setText(bookName);
        btnChapter.setText(chapter);
        btnApply.setEnabled(!moduleID.equals(Librarian.EMPTY_OBJ)
                && !bookID.equals(Librarian.EMPTY_OBJ)
                && !chapter.equals(Librarian.EMPTY_OBJ));
    }

    private void updateView(int viewMode) {
        this.viewMode = viewMode;

        btnModule.setEnabled(viewMode != MODULE_VIEW);
        btnBook.setEnabled(viewMode != BOOK_VIEW);
        btnChapter.setEnabled(viewMode != CHAPTER_VIEW);

        modulesList.setVisibility(viewMode == MODULE_VIEW ? View.VISIBLE : View.GONE);
        booksList.setVisibility(viewMode == BOOK_VIEW ? View.VISIBLE : View.GONE);
        chapterContainer.setVisibility(viewMode == CHAPTER_VIEW ? View.VISIBLE : View.GONE);

        switch (viewMode) {
            case MODULE_VIEW:
                viewModeModule();
                break;
            case BOOK_VIEW:
                viewModeBook();
                break;
            case CHAPTER_VIEW:
                viewModeChapter();
                break;
            default:
                break;
        }
    }

    private void viewModeBook() {
        booksList.setAdapter(getBookAdapter());
        ItemList itemBook;
        try {
            itemBook = new ItemList(bookID, librarian.getBookFullName(moduleID, bookID));
            bookPos = books.indexOf(itemBook);
            if (bookPos >= 0) {
                booksList.setSelection(bookPos);
            }
        } catch (OpenModuleException e) {
            ExceptionHelper.onOpenModuleException(e, this, TAG);
        }
    }

    private void viewModeChapter() {
        chapterList.setAdapter(getChapterAdapter());
        if (chapterPos >= 0) {
            chapterList.setSelection(chapterPos);
        }
    }

    private void viewModeModule() {
        modulesList.setAdapter(getModuleAdapter());
        modulePos = modules.indexOf(new ItemList(moduleID, librarian.getModuleFullName()));
        if (modulePos >= 0) {
            modulesList.setSelection(modulePos);
        }
    }

    private CharSequence getModuleButtonText() {
        if (moduleID.equals(Librarian.EMPTY_OBJ)) {
            return moduleID;
        }

        String moduleName = getModuleNameById(moduleID);
        if (moduleName.isEmpty()) {
            return moduleID;
        }

        String title = moduleID + "\n" + moduleName;
        SpannableString styledTitle = new SpannableString(title);
        int subtitleStart = moduleID.length() + 1;
        styledTitle.setSpan(new RelativeSizeSpan(0.70f),
                subtitleStart,
                title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledTitle.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.library_picker_subtitle)),
                subtitleStart,
                title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styledTitle;
    }

    private String getModuleNameById(String id) {
        for (ItemList module : librarian.getModulesList()) {
            if (id.equals(module.get(ItemList.ID))) {
                return module.get(ItemList.Name);
            }
        }

        return "";
    }
}

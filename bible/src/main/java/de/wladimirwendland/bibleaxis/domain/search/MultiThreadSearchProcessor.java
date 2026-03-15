/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.repository.IModuleRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadSearchProcessor<D, T extends BaseModule> {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int POOL_SIZE = CPU_COUNT * 2 + 1;

    private final IModuleRepository<T> repository;
    private ExecutorService executor;

    public MultiThreadSearchProcessor(IModuleRepository<T> repository) {
        this.repository = repository;
        this.executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    /**
     * Выполняет поиск searchQuery, представляющей из себя регулярное выражение в списке книг bookList
     * модуля module.
     *
     * @param module      модуль, в котором необходимо произвести поиск
     * @param bookList    список id книг модуля
     * @param searchQuery искомый текст в виде регулярного выражения
     *
     * @return возвращает словарь, в котором ключами являются ссылки на место в модуле
     * (см. {@linkplain BibleReference}), а значениями полный текст данного места
     */
    public Map<String, String> search(T module, List<String> bookList, String searchQuery, boolean wholeWordsMatch) {
        Map<String, Future<Map<String, String>>> taskPool = new LinkedHashMap<>();
        for (String bookID : bookList) {
            BookSearchProcessor<D, T> searchProcessor = new BookSearchProcessor<>(repository, module, bookID, searchQuery, wholeWordsMatch);
            SearchThread<D, T> thread = new SearchThread<>(searchProcessor);
            taskPool.put(bookID, executor.submit(thread));
        }

        Map<String, String> searchRes = new LinkedHashMap<>();
        for (Map.Entry<String, Future<Map<String, String>>> entry : taskPool.entrySet()) {
            try {
                searchRes.putAll(entry.getValue().get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return searchRes;
    }

    private static class SearchThread<D, T extends BaseModule> implements Callable<Map<String, String>> {

        private final BookSearchProcessor<D, T> searchProcessor;

        private SearchThread(BookSearchProcessor<D, T> searchProcessor) {
            this.searchProcessor = searchProcessor;
        }

        @Override
        public Map<String, String> call() {
            try {
                return searchProcessor.search();
            } catch (BookNotFoundException e) {
                return new LinkedHashMap<>();
            }
        }
    }
}

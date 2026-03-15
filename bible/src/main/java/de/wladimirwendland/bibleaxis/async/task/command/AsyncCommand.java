/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task.command;

import de.wladimirwendland.bibleaxis.utils.Task;

public class AsyncCommand extends Task {

    private ICommand command;
    private Exception exception;

    public interface ICommand {
        boolean execute() throws Exception;
    }

    public AsyncCommand(ICommand command, String message, Boolean isHidden) {
        super(message, isHidden);
        this.command = command;
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        try {
            return command.execute();
        } catch (Exception e) {
            exception = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
    }

    public Exception getException() {
        return exception;
    }
}

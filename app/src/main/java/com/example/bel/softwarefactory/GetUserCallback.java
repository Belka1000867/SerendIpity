package com.example.bel.softwarefactory;

import com.example.bel.softwarefactory.entities.User;

/**
 * Created by Bel on 21.02.2016.
 *
 * This interface just to get the 'done' method after execution of the Request to server
 * for such reasons that it is possible to implement some functionality after the execution
 * of the Asynchronous tasks
 */
public interface GetUserCallback {

    public abstract void done(User returnedUser);

}

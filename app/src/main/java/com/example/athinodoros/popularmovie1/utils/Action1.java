package com.example.athinodoros.popularmovie1.utils;


import rx.functions.Action;

/**
 * Created by Athinodoros on 3/25/2017.
 */

public interface Action1<T> extends Action {
    void call(T t);
}

package com.bouchov.quiz;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Alexandre Y. Bouchov
 * Date: 25.01.2021
 * Time: 12:39
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class UniqSource {
    private static final AtomicLong number = new AtomicLong();

    public static String uniqueString(String prefix) {
        return prefix + number.incrementAndGet();
    }
}

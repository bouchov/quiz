package com.bouchov.quiz.entities;

import org.junit.jupiter.api.Test;

/**
 * Alexandre Y. Bouchov
 * Date: 05.02.2021
 * Time: 15:29
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class IdGeneratorTest {

    @Test
    public void testAndShow() {
        String generate = IdGenerator.generate();
        System.out.println("generate = " + generate);
    }
}

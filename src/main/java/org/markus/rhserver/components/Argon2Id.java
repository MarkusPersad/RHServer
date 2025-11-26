package org.markus.rhserver.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class Argon2 {
    @Value("${argon2.iterations}")
    private int ITERATIONS;
    @Value("${argon2.memory}")
    private int MEMORY;
    @Value("${argon2.parallelism}")
    private int PARALLELISM;
}

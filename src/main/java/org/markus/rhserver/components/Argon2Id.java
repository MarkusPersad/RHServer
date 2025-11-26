package org.markus.rhserver.components;

import de.mkammerer.argon2.Argon2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Argon2Id {
    @Value("${argon2.iterations}")
    private int ITERATIONS;
    @Value("${argon2.memory}")
    private int MEMORY;
    @Value("${argon2.parallelism}")
    private int PARALLELISM;

    private final Argon2 argon2;
    public Argon2Id(Argon2 argon2){
        this.argon2 = argon2;
    }

    public String encode(@NotNull String password){
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
    }

    public boolean verify(String hash, @NotNull String password){
        return argon2.verify(hash, password.toCharArray());
    }
}

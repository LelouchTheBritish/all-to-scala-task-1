package tinkoff.scala.firsttask.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import tinkoff.scala.firsttask.Client;
import tinkoff.scala.firsttask.Handler;
import tinkoff.scala.firsttask.domain.ApplicationStatusResponse;
import tinkoff.scala.firsttask.domain.Response;

public class DefaultHandler implements Handler {

    private final Client client1;
    private final Client client2;

    public DefaultHandler(Client client1, Client client2) {
        this.client1 = client1;
        this.client2 = client2;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        AtomicBoolean isGotResult = new AtomicBoolean(false);
        AtomicInteger retries = new AtomicInteger(0);
        CompletableFuture
                .supplyAsync(() -> client1.getApplicationStatus1(id))
                .thenApply(res -> {
                    if (isGotResult.get()) {
                        return;
                    }
                    if (res instanceof Response.Success) {
                        isGotResult.compareAndSet(false, true);
                        return new ApplicationStatusResponse.Success(
                                ((Response.Success) res).applicationId(),
                                ((Response.Success) res).applicationId());
                    }
                    if (res instanceof Response.RetryAfter) {
                        retries.getAndIncrement();
                    }
                    if (res instanceof Response.Failure) {
                        return new ApplicationStatusResponse.Failure(null, retries.get());
                    }
                }).orTimeout(15, TimeUnit.SECONDS);
        CompletableFuture
                .supplyAsync(() -> client2.getApplicationStatus1(id))
                .thenApply((res, fr) -> {
                    if (isGotResult.get()) {
                        return;
                    }
                    if (res instanceof Response.Success) {
                        isGotResult.compareAndSet(false, true);
                        return new ApplicationStatusResponse.Success(
                                ((Response.Success) res).applicationId(),
                                ((Response.Success) res).applicationId());
                    }
                    if (res instanceof Response.RetryAfter) {
                        retries.getAndIncrement();
                    }
                    if (res instanceof Response.Failure) {
                        return new ApplicationStatusResponse.Failure(null, retries.get());
                    }
                }).orTimeout(15, TimeUnit.SECONDS);
        return null;
    }
}

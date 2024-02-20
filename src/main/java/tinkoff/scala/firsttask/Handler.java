package tinkoff.scala.firsttask;

import tinkoff.scala.firsttask.domain.ApplicationStatusResponse;

public interface Handler {
    ApplicationStatusResponse performOperation(String id);
}

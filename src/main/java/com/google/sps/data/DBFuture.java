package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import java.util.concurrent.ExecutionException;

public class DBFuture {
  public static Object block(ApiFuture future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }
}
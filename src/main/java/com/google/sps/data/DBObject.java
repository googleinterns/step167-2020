package com.google.sps.meltingpot.data;

// Base class for objects that have an id
// Also nice singleton class in case we need to return just an id
public class DBObject {
  public String id;

  public DBObject(String id) {
    this.id = id;
  }
}
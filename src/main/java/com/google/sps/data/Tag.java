package com.google.sps.meltingpot.data;

public class Tag extends DBObject {
  public static final String HIDDEN_KEY = "hidden";
  public static final String ID_KEY = "id";

  public String name;
  public boolean hidden;
  public String type;

  public Tag() {
    super();
  }

  public Tag(String id) {
    super(id);
  }
  
  public Tag(String id, String name, boolean hidden, String type) {
    super(id);
    this.name = name;
    this.hidden = hidden;
    this.type = type;
  }

  public String toString() {
    return "Tag[id=" + super.id + ", name=" + name + ", hidden=" + String.valueOf(hidden) + ", type=" + type + "}";
  }
}

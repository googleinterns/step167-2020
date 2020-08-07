package com.google.sps.meltingpot.data;

public class Tag extends DBObject {
    public String name;
    public boolean hidden;
    public String type;

    public Tag(){super();}

    public Tag(String id) {
      super(id);
    }
}
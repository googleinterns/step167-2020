package com.google.sps.data;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class DatabaseReferences {
	public static final FirebaseDatabase DB = FirebaseDatabase.getInstance();
	public static final DatabaseReference ROOT = DB.getReference();
	public static final DatabaseReference POSTS = DB.getReference("posts");
}
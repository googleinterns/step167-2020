package com.google.sps.data;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class DBReferences {
	public static final FirebaseDatabase DB = FirebaseDatabase.getInstance();
	public static final DatabaseReference ROOT = DB.getReference();
	public static final DatabaseReference POST_DATA = DB.getReference("post-data");
	public static final DatabaseReference POST_METADATA = DB.getReference("post-metadata");
	public static final DatabaseReference COMMENTS = DB.getReference("comments");
}
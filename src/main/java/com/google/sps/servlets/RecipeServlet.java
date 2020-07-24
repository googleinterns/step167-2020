// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.sps.data.Post;
import com.google.sps.data.PostMetadata;
import com.google.sps.data.Comment;
import com.google.sps.data.DBReferences;

@WebServlet(urlPatterns = "/api/post", asyncSupported = true)
public class RecipeServlet extends HttpServlet
{

    private Gson gson = new Gson();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        final AsyncContext asyncContext = request.startAsync();

        DBReferences.COMMENTS
            .orderByChild(Comment.POST_ID_KEY)
            .equalTo(Integer.parseInt(request.getParameter("id")))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    try{
                        response.getWriter().println(dataSnapshot.toString());
                        System.out.println("Read Succeeded");
                    } catch (Exception e) {
                        System.out.println("IOexception");
                    }
                    asyncContext.complete();
                }
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            }
        );
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Map<String, String> newPost = gson.fromJson(data, new TypeToken<Map<String, String>>(){}.getType());
        if(newPost.get(Post.CONTENT_KEY) == null || newPost.get(PostMetadata.TITLE_KEY) == null)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        DatabaseReference postRef = DBReferences.POST_DATA.push();

        postRef.setValueAsync(Collections.singletonMap(Post.CONTENT_KEY, newPost.get(Post.CONTENT_KEY)));

        String postUid = postRef.getKey();

        DatabaseReference postMetadataRef = DBReferences.POST_METADATA.child(postUid);
        postMetadataRef.setValueAsync(Collections.singletonMap(PostMetadata.TITLE_KEY, newPost.get(PostMetadata.TITLE_KEY)));

        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException {}
}

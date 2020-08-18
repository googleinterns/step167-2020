package com.google.sps.meltingpot.servlets;

import javax.servlet.http.HttpServletResponse;

public class Cors {
  public static void setCors(HttpServletResponse response) {
    response.addHeader("Access-Control-Allow-Origin", "http://localhost:3001");
  }
}

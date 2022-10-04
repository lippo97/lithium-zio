package org.github.lithium.http

import java.net.URLEncoder

def encodeURIComponent(value: String): String = 
  URLEncoder.encode(value, "UTF-8")

def encodeURI(body: Map[String, String]): String = 
  body.map((key, value) => s"${key}=${encodeURIComponent(value)}").mkString("&")

def encodeURI(body: (String, String)*): String = encodeURI(body.toMap)

def encodeURIParams(base: String, params: Map[String, String]): String = 
  s"${base}?${encodeURI(params)}"

def encodeURIParams(base: String, params: (String, String)*): String = 
  encodeURIParams(base, params.toMap)

implicit def longEncoder(long: Long): String = long.toString()

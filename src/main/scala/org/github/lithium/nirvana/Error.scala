package org.github.lithium.nirvana

enum Error:
  case InvalidAuthtoken extends Error
  case FetchError extends Error
  case ParseError(message: String) extends Error

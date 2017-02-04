package org.folio.metadata.common

interface WebContext extends Context {
  def URL absoluteUrl(String path)
}

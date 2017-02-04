package org.folio.inventory.resources

import org.folio.metadata.common.WebContext

class FakeWebContext implements WebContext {
  @Override
  String getTenantId() {
    null
  }

  @Override
  String getOkapiLocation() {
    null
  }

  @Override
  def getHeader(String header) {
    null
  }

  @Override
  def getHeader(String header, Object defaultValue) {
    null
  }

  @Override
  boolean hasHeader(String header) {
    false
  }

  @Override
  URL absoluteUrl(String path) {
    new URL("http://localhost/${path}")
  }
}

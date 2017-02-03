package org.folio.inventory.domain

class Instance {
  final String id
  final String title
  final List<Map> identifiers
  final String publicationDate

  def Instance(String title, List<Map> identifiers) {
    this(null, title, identifiers)
  }

  def Instance(String title, String publicationDate) {
    this(null, title, [], publicationDate)
  }

  def Instance(String id, String title, List<Map> identifiers) {
    this.id = id
    this.title = title
    this.identifiers = identifiers.collect()
  }

  def Instance(
    String id,
    String title,
    List<Map> identifiers,
    String publicationDate) {

    this.id = id
    this.title = title
    this.identifiers = identifiers.collect()
    this.publicationDate = publicationDate
  }

  def Instance copyWithNewId(String newId) {
    new Instance(newId, this.title, this.identifiers, this.publicationDate)
  }

  Instance addIdentifier(Map identifier) {
    new Instance(id, title, this.identifiers.collect() << identifier)
  }

  Instance addIdentifier(String namespace, String value) {
    def identifier = ['namespace' : namespace, 'value' : value]

    new Instance(id, title, this.identifiers.collect() << identifier,
      publicationDate)
  }

  @Override
  public String toString() {
    println ("Instance ID: ${id}, Title: ${title}")
  }
}

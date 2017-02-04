package org.folio.inventory.resources

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.folio.inventory.domain.Instance
import org.folio.metadata.common.WebContext

class InstanceRepresentation {
  private final String relativeInstancesPath

  def InstanceRepresentation(String relativeInstancesPath) {
    this.relativeInstancesPath = relativeInstancesPath
  }

  private JsonObject toJson(List<Instance> instances, WebContext context) {
    def representation = new JsonObject()

    def results = new JsonArray()

    instances.each {
      results.add(toJson(it, context))
    }

    representation.put("instances", results)

    representation
  }

  private JsonObject toJson(Instance instance, WebContext context) {
    def representation = new JsonObject()

    representation.put("@context", context.absoluteUrl(
       "${relativeInstancesPath}/context").toString())

    representation.put("id", instance.id)
    representation.put("title", instance.title)

    if(instance.publicationDate != null) {
      representation.put("publication", new JsonObject()
        .put("date", instance.publicationDate))
    }

    def identifiers = []

    instance.identifiers.each { identifier ->
      def identifierRepresentation = [:]

      identifierRepresentation.namespace = identifier.namespace
      identifierRepresentation.value = identifier.value

      identifiers.add(identifierRepresentation)
    }

    representation.put('identifiers', identifiers)

    representation.put('links',
      ['self': context.absoluteUrl(
         "${relativeInstancesPath}/${instance.id}").toString()])

    representation
  }
}

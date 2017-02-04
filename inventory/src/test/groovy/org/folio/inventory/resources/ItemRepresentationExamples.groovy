package org.folio.inventory.resources

import org.folio.inventory.domain.Instance
import org.folio.inventory.domain.Item
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.hamcrest.junit.MatcherAssert.assertThat

class ItemRepresentationExamples {

  @Test
  void nullPublicationDateShouldNotBePresented() {
    def instance = new Instance(UUID.randomUUID().toString(), "Nod", [])
    def item = new Item(instance.title, "5646536543", instance.id)

    def representation = new ItemRepresentation("/items")
      .toJson(item, instance, new FakeWebContext())

    assertThat(representation.containsKey("publicationDate"), is(false))
  }

}

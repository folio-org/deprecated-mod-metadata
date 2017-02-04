package org.folio.inventory.resources

import org.folio.inventory.domain.Instance
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.hamcrest.junit.MatcherAssert.assertThat

class InstanceRepresentationExamples {

  @Test
  void nullPublicationDateShouldNotBePresented() {
    def instance = new Instance(UUID.randomUUID().toString(), "Nod", [])

    def representation = new InstanceRepresentation("/instances")
      .toJson(instance, new FakeWebContext())

    assertThat(representation.containsKey("publication"), is(false))
  }

}

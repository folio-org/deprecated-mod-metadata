package org.folio.inventory.storage.memory

import org.folio.inventory.domain.Item
import org.folio.inventory.domain.ItemCollection
import org.folio.metadata.common.api.request.PagingParameters
import org.folio.metadata.common.domain.Failure
import org.folio.metadata.common.domain.Success
import org.folio.metadata.common.storage.memory.InMemoryCollection

import java.util.function.Consumer

class InMemoryItemCollection
  implements ItemCollection {

  private final collection = new InMemoryCollection<Item>()

  @Override
  void add(Item item,
           Consumer<Success<Item>> resultCallback,
           Consumer<Failure> failureCallback) {
    def id = item.id ?: UUID.randomUUID().toString()

    collection.add(item.copyWithNewId(id), resultCallback)
  }

  @Override
  void findById(String id,
                Consumer<Success<Item>> resultCallback,
                Consumer<Failure> failureCallback) {
    collection.findOne({ it.id == id }, resultCallback)
  }

  @Override
  void findAll(PagingParameters pagingParameters,
               Consumer<Success<Map>> resultCallback,
               Consumer<Failure> failureCallback) {
    collection.some(pagingParameters, "items", resultCallback)
  }

  @Override
  void empty(Consumer<Success> completionCallback,
             Consumer<Failure> failureCallback) {
    collection.empty(completionCallback)
  }

  @Override
  void findByCql(String cqlQuery,
                 PagingParameters pagingParameters,
                 Consumer<Success<Map>> resultCallback,
                 Consumer<Failure> failureCallback) {

    collection.find(cqlQuery, pagingParameters, "items", resultCallback)
  }

  @Override
  void update(Item item,
              Consumer<Success> completionCallback,
              Consumer<Failure> failureCallback) {

    collection.replace(item, completionCallback)
  }

  @Override
  void delete(String id,
              Consumer<Success> completionCallback,
              Consumer<Failure> failureCallback) {
    collection.remove(id, completionCallback)
  }
}

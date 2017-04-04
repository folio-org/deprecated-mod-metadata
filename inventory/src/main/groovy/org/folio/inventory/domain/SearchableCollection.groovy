package org.folio.inventory.domain

import org.folio.metadata.common.api.request.PagingParameters
import org.folio.metadata.common.domain.Failure
import org.folio.metadata.common.domain.Success

import java.util.function.Consumer

interface SearchableCollection<T> {
  void findByCql(String cqlQuery,
                 PagingParameters pagingParameters,
                 Consumer<Success<Map>> resultsCallback,
                 Consumer<Failure> failureCallback)
}

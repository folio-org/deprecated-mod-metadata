package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Items;
import org.folio.rest.jaxrs.resource.ItemStorageResource;
import org.folio.rest.tools.utils.OutStream;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.tools.utils.TenantTool;

public class ItemStorageAPI implements ItemStorageResource {

  // Has to be lowercase because raml-module-builder uses case sensitive
  // lower case headers
  private static final String TENANT_HEADER = "x-okapi-tenant";
  private static final String BLANK_TENANT_MESSAGE = "Tenant Must Be Provided";

  // Replace the replaced IDs
  private static final Map<String, String> replacedToOriginalIdMap = new HashMap<>();

  @Override
  public void getItemStorageItem(@DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    Criteria a = new Criteria();
    Criterion criterion = new Criterion(a);

    try {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {

        try {
          postgresClient.get("item", Item.class, criterion, false,
            reply -> {
              try {
                List<Item> items = (List<Item>) reply.result()[0];

                items.forEach( item -> {
                  putBackReplacedId(item);
                });

                Items itemList = new Items();
                itemList.setItems(items);
                itemList.setTotalRecords(items.size());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemResponse.
                    withJsonOK(itemList)));

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemResponse.
                    withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.GetItemStorageItemResponse.
              withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.GetItemStorageItemResponse.
          withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void postItemStorageItem(@DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang, Item entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    try {
      PostgresClient postgresClient =
        PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {
        try {

          postgresClient.save("item", entity,
            reply -> {
              try {
                replacedToOriginalIdMap.put(reply.result(), entity.getId());

                OutStream stream = new OutStream();
                stream.setData(entity);

                asyncResultHandler.handle(
                  io.vertx.core.Future.succeededFuture(
                    ItemStorageResource.PostItemStorageItemResponse
                      .withJsonCreated(reply.result(), stream)));

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(
                  io.vertx.core.Future.succeededFuture(
                    ItemStorageResource.PostItemStorageItemResponse
                      .withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.PostItemStorageItemResponse
              .withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.PostItemStorageItemResponse
          .withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void postItemStorageItemByItemId(
    @PathParam("itemId")
    @NotNull
      String itemId,
    @QueryParam("lang")
    @DefaultValue("en")
    @Pattern(regexp = "[a-zA-Z]{2}")
      String lang, java.util.Map<String, String> okapiHeaders, io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

  }

  @Override
  public void getItemStorageItemByItemId(
    @PathParam("itemId")
    @NotNull
      String itemId,
    @QueryParam("lang")
    @DefaultValue("en")
    @Pattern(regexp = "[a-zA-Z]{2}")
      String lang, java.util.Map<String, String> okapiHeaders, io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    Criteria a = new Criteria();

    a.addField("'id'");
    a.setOperation("=");
    a.setValue(itemId);

    Criterion criterion = new Criterion(a);

    try {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.get("item", Item.class, criterion, false,
            reply -> {
              try {
                List<Item> itemList = (List<Item>) reply.result()[0];
                if (itemList.size() == 1) {
                  Item item = itemList.get(0);

                  putBackReplacedId(item);

                  asyncResultHandler.handle(
                    io.vertx.core.Future.succeededFuture(
                      ItemStorageResource.GetItemStorageItemByItemIdResponse.
                        withJsonOK(item)));
                } else {
                  throw new Exception(itemList.size() + " results returned");
                }

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemByItemIdResponse.
                    withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.GetItemStorageItemByItemIdResponse.
              withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.GetItemStorageItemByItemIdResponse.
          withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void deleteItemStorageItem(
    @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    vertxContext.runOnContext(v -> {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      postgresClient.mutate("TRUNCATE TABLE item",
          reply -> {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              ItemStorageResource.DeleteItemStorageItemResponse.ok().build()));
          });
      });
  }

  @Override
  public void putItemStorageItemByItemId(
    @PathParam("itemId")
    @NotNull
      String itemId,
    @QueryParam("lang")
    @DefaultValue("en")
    @Pattern(regexp = "[a-zA-Z]{2}")
      String lang, Item entity, java.util.Map<String, String> okapiHeaders, io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

  }

  @Override
  public void deleteItemStorageItemByItemId(
    @PathParam("itemId")
    @NotNull
      String itemId,
    @QueryParam("lang")
    @DefaultValue("en")
    @Pattern(regexp = "[a-zA-Z]{2}")
      String lang, java.util.Map<String, String> okapiHeaders, io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

  }

  private void putBackReplacedId(Item item) {
    if(replacedToOriginalIdMap.containsKey(item.getId())) {
      item.setId(replacedToOriginalIdMap.get(item.getId()));
    }
  }

  private void badRequestResult(
    Handler<AsyncResult<Response>> asyncResultHandler, String message) {
    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
      GetItemStorageItemResponse.withPlainBadRequest(message)));
  }

  private boolean blankTenantId(String tenantId) {
    return tenantId == null || tenantId == "" || tenantId == "folio_shared";
  }
}

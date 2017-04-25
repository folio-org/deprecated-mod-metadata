package org.folio.rest;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.rest.support.*;
import org.junit.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class ItemStorageTest {

  private static HttpClient client = new HttpClient(StorageTestSuite.getVertx());

  private static String mtPostRequest = "{\"name\": \"journal\"}";

  private static String materialTypeID;

  @BeforeClass
  public static void beforeAny() {
    try {
      createMT();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Before
  public void beforeEach()
    throws InterruptedException,
    ExecutionException,
    TimeoutException,
    MalformedURLException {

    StorageTestSuite.deleteAll(itemStorageUrl());

  }

  @After
  public void checkIdsAfterEach()
    throws InterruptedException, ExecutionException, TimeoutException {

    StorageTestSuite.checkForMismatchedIDs("item");
  }

  @Test
  public void canCreateAnItemViaCollectionResource()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = nod(id, instanceId);

    CompletableFuture<JsonResponse> createCompleted = new CompletableFuture();

    client.post(itemStorageUrl(), itemToCreate, StorageTestSuite.TENANT_ID,
      ResponseHandler.json(createCompleted));

    JsonResponse postResponse = createCompleted.get(5, TimeUnit.SECONDS);

    assertThat(postResponse.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));

    JsonObject itemFromPost = postResponse.getJson();

    assertThat(itemFromPost.getString("id"), is(id.toString()));
    assertThat(itemFromPost.getString("instanceId"), is(instanceId.toString()));
    assertThat(itemFromPost.getString("title"), is("Nod"));
    assertThat(itemFromPost.getString("barcode"), is("565578437802"));
    assertThat(itemFromPost.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(itemFromPost.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(itemFromPost.getJsonObject("location").getString("name"),
      is("Main Library"));

    JsonResponse getResponse = getById(id);

    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject itemFromGet = getResponse.getJson();

    assertThat(itemFromGet.getString("id"), is(id.toString()));
    assertThat(itemFromGet.getString("instanceId"), is(instanceId.toString()));
    assertThat(itemFromGet.getString("title"), is("Nod"));
    assertThat(itemFromGet.getString("barcode"), is("565578437802"));
    assertThat(itemFromGet.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(itemFromGet.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(itemFromGet.getJsonObject("location").getString("name"),
      is("Main Library"));
  }

  @Test
  public void canCreateAnItemWithoutProvidingID()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = nod(null, instanceId);

    CompletableFuture<JsonResponse> createCompleted = new CompletableFuture();

    client.post(itemStorageUrl(), itemToCreate, StorageTestSuite.TENANT_ID,
      ResponseHandler.json(createCompleted));

    JsonResponse postResponse = createCompleted.get(5, TimeUnit.SECONDS);

    assertThat(postResponse.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));

    JsonObject itemFromPost = postResponse.getJson();

    String newId = itemFromPost.getString("id");

    assertThat(newId, is(notNullValue()));

    JsonResponse getResponse = getById(UUID.fromString(newId));

    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject itemFromGet = getResponse.getJson();

    assertThat(itemFromGet.getString("id"), is(newId));
    assertThat(itemFromGet.getString("instanceId"), is(instanceId.toString()));
    assertThat(itemFromGet.getString("title"), is("Nod"));
    assertThat(itemFromGet.getString("barcode"), is("565578437802"));
    assertThat(itemFromGet.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(itemFromGet.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(itemFromGet.getJsonObject("location").getString("name"),
      is("Main Library"));
  }

  @Test
  public void cannotCreateAnItemWithIDThatIsNotUUID()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    String id = "1234";
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = new JsonObject();

    itemToCreate.put("id", id.toString());
    itemToCreate.put("instanceId", instanceId.toString());
    itemToCreate.put("title", "Nod");
    itemToCreate.put("barcode", "565578437802");
    itemToCreate.put("status", new JsonObject().put("name", "Available"));
    itemToCreate.put("materialTypeId", materialTypeID);
    itemToCreate.put("location", new JsonObject().put("name", "Main Library"));

    CompletableFuture<TextResponse> createCompleted = new CompletableFuture();

    client.post(itemStorageUrl(), itemToCreate, StorageTestSuite.TENANT_ID,
      ResponseHandler.text(createCompleted));

    TextResponse postResponse = createCompleted.get(5, TimeUnit.SECONDS);

    assertThat(postResponse.getStatusCode(), is(HttpURLConnection.HTTP_BAD_REQUEST));

    assertThat(postResponse.getBody(), is("ID and instance ID must both be a UUID"));
  }

  @Test
  public void canCreateAnItemWithoutMaterialType()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = new JsonObject();

    itemToCreate.put("id", id.toString());
    itemToCreate.put("instanceId", instanceId.toString());
    itemToCreate.put("title", "Nod");
    itemToCreate.put("barcode", "565578437802");
    itemToCreate.put("status", new JsonObject().put("name", "Available"));
    itemToCreate.put("location", new JsonObject().put("name", "Main Library"));

    CompletableFuture<JsonResponse> createCompleted = new CompletableFuture();

    client.post(itemStorageUrl(), itemToCreate, StorageTestSuite.TENANT_ID,
      ResponseHandler.json(createCompleted));

    JsonResponse postResponse = createCompleted.get(5, TimeUnit.SECONDS);

    assertThat(postResponse.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));

    JsonObject itemFromPost = postResponse.getJson();

    assertThat(itemFromPost.getString("id"), is(id.toString()));
    assertThat(itemFromPost.getString("instanceId"), is(instanceId.toString()));
    assertThat(itemFromPost.getString("title"), is("Nod"));
    assertThat(itemFromPost.getString("barcode"), is("565578437802"));
    assertThat(itemFromPost.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(itemFromPost.getJsonObject("location").getString("name"),
      is("Main Library"));

    JsonResponse getResponse = getById(id);

    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject itemFromGet = getResponse.getJson();

    assertThat(itemFromGet.getString("id"), is(id.toString()));
    assertThat(itemFromGet.getString("instanceId"), is(instanceId.toString()));
    assertThat(itemFromGet.getString("title"), is("Nod"));
    assertThat(itemFromGet.getString("barcode"), is("565578437802"));
    assertThat(itemFromGet.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(itemFromGet.getJsonObject("location").getString("name"),
      is("Main Library"));
  }

  @Test
  public void canCreateAnItemAtSpecificLocation()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = nod(id, instanceId);

    CompletableFuture<Response> createCompleted = new CompletableFuture();

    client.put(itemStorageUrl(String.format("/%s", id)), itemToCreate,
      StorageTestSuite.TENANT_ID, ResponseHandler.empty(createCompleted));

    Response putResponse = createCompleted.get(5, TimeUnit.SECONDS);

    assertThat(putResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    JsonResponse getResponse = getById(id);

    //PUT currently cannot return a response
    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject item = getResponse.getJson();

    assertThat(item.getString("id"), is(id.toString()));
    assertThat(item.getString("instanceId"), is(instanceId.toString()));
    assertThat(item.getString("title"), is("Nod"));
    assertThat(item.getString("barcode"), is("565578437802"));
    assertThat(item.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(item.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(item.getJsonObject("location").getString("name"),
      is("Main Library"));
  }

  @Test
  public void canReplaceAnItemAtSpecificLocation()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = smallAngryPlanet(id, instanceId);

    createItem(itemToCreate);

    JsonObject replacement = itemToCreate.copy();
      replacement.put("barcode", "125845734657");
      replacement.put("location",
        new JsonObject().put("name", "Annex Library"));

    CompletableFuture<Response> replaceCompleted = new CompletableFuture();

    client.put(itemStorageUrl(String.format("/%s", id)), replacement,
      StorageTestSuite.TENANT_ID, ResponseHandler.empty(replaceCompleted));

    Response putResponse = replaceCompleted.get(5, TimeUnit.SECONDS);

    assertThat(putResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    JsonResponse getResponse = getById(id);

    //PUT currently cannot return a response
    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject item = getResponse.getJson();

    assertThat(item.getString("id"), is(id.toString()));
    assertThat(item.getString("instanceId"), is(instanceId.toString()));
    assertThat(item.getString("title"), is("Long Way to a Small Angry Planet"));
    assertThat(item.getString("barcode"), is("125845734657"));
    assertThat(item.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(item.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(item.getJsonObject("location").getString("name"),
      is("Annex Library"));
  }

  @Test
  public void canReplaceAnItemWithASingleQuoteInTheTitle()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = createItemRequest(id, instanceId,
      "The Time Traveller's Wife", "036587275931");

    createItem(itemToCreate);

    JsonObject replacement = itemToCreate.copy();
    replacement.put("barcode", "036587275931");
    replacement.put("location",
      new JsonObject().put("name", "Annex Library"));

    CompletableFuture<Response> replaceCompleted = new CompletableFuture();

    client.put(itemStorageUrl(String.format("/%s", id)), replacement,
      StorageTestSuite.TENANT_ID, ResponseHandler.empty(replaceCompleted));

    Response putResponse = replaceCompleted.get(5, TimeUnit.SECONDS);

    assertThat(putResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    JsonResponse getResponse = getById(id);

    //PUT currently cannot return a response
    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_OK));

    JsonObject item = getResponse.getJson();

    assertThat(item.getString("id"), is(id.toString()));
    assertThat(item.getString("instanceId"), is(instanceId.toString()));
    assertThat(item.getString("title"), is("The Time Traveller's Wife"));
    assertThat(item.getString("barcode"), is("036587275931"));
    assertThat(item.getJsonObject("status").getString("name"),
      is("Available"));
    assertThat(item.getString("materialTypeId"),
      is(materialTypeID));
    assertThat(item.getJsonObject("location").getString("name"),
      is("Annex Library"));
  }

  @Test
  public void canDeleteAnItem() throws InterruptedException,
    MalformedURLException, TimeoutException, ExecutionException {

    UUID id = UUID.randomUUID();
    UUID instanceId = UUID.randomUUID();

    JsonObject itemToCreate = smallAngryPlanet(id, instanceId);

    createItem(itemToCreate);

    CompletableFuture<Response> deleteCompleted = new CompletableFuture();

    client.delete(itemStorageUrl(String.format("/%s", id)),
      StorageTestSuite.TENANT_ID, ResponseHandler.empty(deleteCompleted));

    Response deleteResponse = deleteCompleted.get(5, TimeUnit.SECONDS);

    assertThat(deleteResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    CompletableFuture<Response> getCompleted = new CompletableFuture();

    client.get(itemStorageUrl(String.format("/%s", id)),
      StorageTestSuite.TENANT_ID, ResponseHandler.empty(getCompleted));

    Response getResponse = getCompleted.get(5, TimeUnit.SECONDS);

    assertThat(getResponse.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
  }

  @Test
  public void canPageAllItems()
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    createItem(smallAngryPlanet());
    createItem(nod());
    createItem(uprooted());
    createItem(temeraire());
    createItem(interestingTimes());

    CompletableFuture<JsonResponse> firstPageCompleted = new CompletableFuture();
    CompletableFuture<JsonResponse> secondPageCompleted = new CompletableFuture();

    client.get(itemStorageUrl() + "?limit=3", StorageTestSuite.TENANT_ID,
      ResponseHandler.json(firstPageCompleted));

    client.get(itemStorageUrl() + "?limit=3&offset=3", StorageTestSuite.TENANT_ID,
      ResponseHandler.json(secondPageCompleted));

    JsonResponse firstPageResponse = firstPageCompleted.get(5, TimeUnit.SECONDS);
    JsonResponse secondPageResponse = secondPageCompleted.get(5, TimeUnit.SECONDS);

    assertThat(firstPageResponse.getStatusCode(), is(200));
    assertThat(secondPageResponse.getStatusCode(), is(200));

    JsonObject firstPage = firstPageResponse.getJson();
    JsonObject secondPage = secondPageResponse.getJson();

    JsonArray firstPageItems = firstPage.getJsonArray("items");
    JsonArray secondPageItems = secondPage.getJsonArray("items");

    assertThat(firstPageItems.size(), is(3));
    assertThat(firstPage.getInteger("totalRecords"), is(5));

    assertThat(secondPageItems.size(), is(2));
    assertThat(secondPage.getInteger("totalRecords"), is(5));
  }

  @Test
  public void canSearchForItemsByTitle()
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    createItem(smallAngryPlanet());
    createItem(nod());
    createItem(uprooted());
    createItem(temeraire());
    createItem(interestingTimes());

    CompletableFuture<JsonResponse> searchCompleted = new CompletableFuture();

    String url = itemStorageUrl() + "?query=title=\"*Up*\"";

    client.get(url,
      StorageTestSuite.TENANT_ID, ResponseHandler.json(searchCompleted));

    JsonResponse searchResponse = searchCompleted.get(5, TimeUnit.SECONDS);

    assertThat(searchResponse.getStatusCode(), is(200));

    JsonObject searchBody = searchResponse.getJson();

    JsonArray foundItems = searchBody.getJsonArray("items");

    assertThat(foundItems.size(), is(1));
    assertThat(searchBody.getInteger("totalRecords"), is(1));

    assertThat(foundItems.getJsonObject(0).getString("title"),
      is("Uprooted"));
  }

  @Test
  public void canSearchForItemsByBarcode()
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    createItem(smallAngryPlanet());
    createItem(nod());
    createItem(uprooted());
    createItem(temeraire());
    createItem(interestingTimes());

    CompletableFuture<JsonResponse> searchCompleted = new CompletableFuture();

    String url = itemStorageUrl() + "?query=barcode=036000291452";

    client.get(url,
      StorageTestSuite.TENANT_ID, ResponseHandler.json(searchCompleted));

    JsonResponse searchResponse = searchCompleted.get(5, TimeUnit.SECONDS);

    assertThat(searchResponse.getStatusCode(), is(200));

    JsonObject searchBody = searchResponse.getJson();

    JsonArray foundItems = searchBody.getJsonArray("items");

    assertThat(foundItems.size(), is(1));
    assertThat(searchBody.getInteger("totalRecords"), is(1));

    assertThat(foundItems.getJsonObject(0).getString("title"),
      is("Long Way to a Small Angry Planet"));
  }

  @Test
  public void cannotSearchForItemsUsingADefaultField()
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    createItem(smallAngryPlanet());
    createItem(nod());
    createItem(uprooted());
    createItem(temeraire());
    createItem(interestingTimes());

    CompletableFuture<TextResponse> searchCompleted = new CompletableFuture();

    String url = itemStorageUrl() + "?query=t";

    client.get(url,
      StorageTestSuite.TENANT_ID, ResponseHandler.text(searchCompleted));

    TextResponse searchResponse = searchCompleted.get(5, TimeUnit.SECONDS);

    assertThat(searchResponse.getStatusCode(), is(500));

    String error = searchResponse.getBody();

    assertThat(error,
      is("CQL State Error for 't': org.z3950.zing.cql.cql2pgjson.QueryValidationException: cql.serverChoice requested, but no serverChoiceIndexes defined."));
  }

  @Test
  public void canDeleteAllItems()
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    createItem(smallAngryPlanet());
    createItem(nod());
    createItem(uprooted());
    createItem(temeraire());
    createItem(interestingTimes());

    CompletableFuture<Response> deleteAllFinished = new CompletableFuture();

    client.delete(itemStorageUrl(), StorageTestSuite.TENANT_ID,
      ResponseHandler.empty(deleteAllFinished));

    Response deleteResponse = deleteAllFinished.get(5, TimeUnit.SECONDS);

    assertThat(deleteResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    CompletableFuture<JsonResponse> getCompleted = new CompletableFuture();

    client.get(itemStorageUrl(), StorageTestSuite.TENANT_ID,
      ResponseHandler.json(getCompleted));

    JsonResponse response = getCompleted.get(5, TimeUnit.SECONDS);

    JsonObject responseBody = response.getJson();

    JsonArray allItems = responseBody.getJsonArray("items");

    assertThat(allItems.size(), is(0));
    assertThat(responseBody.getInteger("totalRecords"), is(0));
  }

  @Test
  public void tenantIsRequiredForCreatingNewItem()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    CompletableFuture<TextResponse> postCompleted = new CompletableFuture();

    client.post(itemStorageUrl(), smallAngryPlanet(),
      ResponseHandler.text(postCompleted));

    TextResponse response = postCompleted.get(5, TimeUnit.SECONDS);

    assertThat(response.getStatusCode(), is(400));
    assertThat(response.getBody(), is("Tenant Must Be Provided"));
  }

  @Test
  public void tenantIsRequiredForGettingAnItem()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    URL getInstanceUrl = itemStorageUrl(String.format("/%s",
      UUID.randomUUID().toString()));

    CompletableFuture<TextResponse> getCompleted = new CompletableFuture();

    client.get(getInstanceUrl, ResponseHandler.text(getCompleted));

    TextResponse response = getCompleted.get(5, TimeUnit.SECONDS);

    assertThat(response.getStatusCode(), is(400));
    assertThat(response.getBody(), is("Tenant Must Be Provided"));
  }

  @Test
  public void tenantIsRequiredForGettingAllItems()
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    CompletableFuture<TextResponse> getCompleted = new CompletableFuture();

    client.get(itemStorageUrl(), ResponseHandler.text(getCompleted));

    TextResponse response = getCompleted.get(5, TimeUnit.SECONDS);

    assertThat(response.getStatusCode(), is(400));
    assertThat(response.getBody(), is("Tenant Must Be Provided"));
  }

  private JsonResponse getById(UUID id)
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    URL getItemUrl = itemStorageUrl(String.format("/%s", id));

    CompletableFuture<JsonResponse> getCompleted = new CompletableFuture();

    client.get(getItemUrl, StorageTestSuite.TENANT_ID,
      ResponseHandler.json(getCompleted));

    return getCompleted.get(5, TimeUnit.SECONDS);
  }

  private void createItem(JsonObject itemToCreate)
    throws MalformedURLException, InterruptedException,
    ExecutionException, TimeoutException {

    CompletableFuture<TextResponse> createCompleted = new CompletableFuture();

    try {
      client.post(itemStorageUrl(), itemToCreate, StorageTestSuite.TENANT_ID,
        ResponseHandler.text(createCompleted));

      TextResponse response = createCompleted.get(2, TimeUnit.SECONDS);

      if (response.getStatusCode() != 201) {
        System.out.println("WARNING!!!!! Create item preparation failed: "
          + response.getBody());
      }
    }
    catch(Exception e) {
      System.out.println("WARNING!!!!! Create item preparation failed: "
        + e.getMessage());
    }
  }

  private static URL getMTUrl() throws MalformedURLException {
    return StorageTestSuite.storageUrl("/material-types");
  }

  private static URL itemStorageUrl() throws MalformedURLException {
    return itemStorageUrl("");
  }

  private static URL itemStorageUrl(String subPath)
    throws MalformedURLException {

    return StorageTestSuite.storageUrl("/item-storage/items" + subPath);
  }

  private JsonObject createItemRequest(
    UUID id,
    UUID instanceId,
    String title,
    String barcode) {

    JsonObject itemToCreate = new JsonObject();

    if(id != null) {
      itemToCreate.put("id", id.toString());
    }

    itemToCreate.put("instanceId", instanceId.toString());
    itemToCreate.put("title", title);
    itemToCreate.put("barcode", barcode);
    itemToCreate.put("status", new JsonObject().put("name", "Available"));
    itemToCreate.put("materialTypeId", materialTypeID);
    itemToCreate.put("location", new JsonObject().put("name", "Main Library"));

    return itemToCreate;
  }

  private JsonObject smallAngryPlanet(UUID itemId, UUID instanceId) {
    return createItemRequest(itemId, instanceId,
      "Long Way to a Small Angry Planet", "036000291452");
  }

  private JsonObject smallAngryPlanet() {
    return smallAngryPlanet(UUID.randomUUID(), UUID.randomUUID());
  }

  private JsonObject nod(UUID itemId, UUID instanceId) {
    return createItemRequest(itemId, instanceId,
      "Nod", "565578437802");
  }

  private JsonObject nod() {
    return nod(UUID.randomUUID(), UUID.randomUUID());
  }

  private JsonObject uprooted() {
    return createItemRequest(UUID.randomUUID(), UUID.randomUUID(),
      "Uprooted", "657670342075");
  }

  private JsonObject temeraire() {
    return createItemRequest(UUID.randomUUID(), UUID.randomUUID(),
      "Temeraire", "232142443432");
  }

  private JsonObject interestingTimes() {
    return createItemRequest(UUID.randomUUID(), UUID.randomUUID(),
      "Interesting Times", "56454543534");
  }

  private static void createMT() throws Exception {
    CompletableFuture<JsonResponse> mtCreateCompleted = new CompletableFuture();
    client.post(getMTUrl(), new JsonObject(mtPostRequest), StorageTestSuite.TENANT_ID,
      ResponseHandler.json(mtCreateCompleted));
    JsonResponse mtPostResponse = mtCreateCompleted.get(5, TimeUnit.SECONDS);
    materialTypeID = mtPostResponse.getJson().getString("id");
  }
}

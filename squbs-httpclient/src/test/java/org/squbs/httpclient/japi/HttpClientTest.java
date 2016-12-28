/*
 *  Copyright 2015 PayPal
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.squbs.httpclient.japi;

import akka.actor.ActorSystem;
import akka.http.javadsl.HostConnectionPool;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.junit.AfterClass;
import org.junit.Test;
import org.squbs.endpoint.EndpointResolverRegistry;
import org.squbs.httpclient.ClientFlow;
import org.squbs.httpclient.dummy.DummyServiceEndpointResolver;
import org.squbs.httpclient.dummy.DummyServiceJavaTest;
import scala.concurrent.Await;
import scala.util.Try;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.squbs.testkit.Timeouts.awaitMax;

public class HttpClientTest {

    private static final ActorSystem system = ActorSystem.create("HttpClientTest");
    private final ActorMaterializer materializer = ActorMaterializer.create(system);
    private final DummyServiceJavaTest dummyService;
    private final String baseUrl;
    private final Flow<Pair<HttpRequest, Integer>, Pair<Try<HttpResponse>, Integer>, HostConnectionPool> clientFlow;

    public HttpClientTest() throws Exception {
        dummyService = new DummyServiceJavaTest();
        final int port = (Integer) Await.result(dummyService.startService(system), awaitMax());
        baseUrl = "http://localhost:" + port;
        EndpointResolverRegistry.get(system).register(new DummyServiceEndpointResolver(baseUrl, system));
        clientFlow = ClientFlow.create("DummyService", system, materializer);
    }

    CompletionStage<Try<HttpResponse>> doRequest(HttpRequest request) {
        return Source
                .single(Pair.create(request, 42))
                .via(clientFlow)
                .runWith(Sink.head(), materializer)
                .thenApply(Pair::first);
    }

    @AfterClass
    public static void afterAll() {
        system.terminate();
    }

    @Test
    public void testGetCaseClass() throws Exception {
        CompletionStage<Try<HttpResponse>> tryResponse =
                doRequest(HttpRequest.GET("/view"));
        CompletionStage<HttpEntity.Strict> entity =
                tryResponse.thenCompose(t -> t.get().entity().toStrict(30000L, materializer));
        HttpResponse response = tryResponse.toCompletableFuture().get().get();
        assertEquals(StatusCodes.OK, response.status());
        String content = entity.toCompletableFuture().get().getData().utf8String();
        assertEquals(dummyService.fullTeamJson(), content);
    }


/*


    public static Future<HttpResponse> rawGet(String clientName, ActorSystem system, String uri) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().get(uri);
    }

    public static Future<HttpResponse> rawGet(String clientName, ActorSystem system, String uri,
                                              RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().get(uri, reqSetting);
    }

    public static <T> Future<HttpResponse> rawPost(String clientName, ActorSystem system, String uri,
                                                   Optional<T> data) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().post(uri, data);
    }

    public static <T> Future<HttpResponse>
    rawPost(String clientName, ActorSystem system, String uri, Optional<T> data, RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().post(uri, data, reqSetting);
    }

    public static <T> Future<HttpResponse> rawPost(String clientName, ActorSystem system, String uri, Optional<T> data,
                                                   Marshaller<T> marshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().post(uri, data, marshaller);
    }

    public static <T> Future<HttpResponse> rawPost(String clientName, ActorSystem system, String uri, Optional<T> data,
                                                   RequestSettings reqSetting, Marshaller<T> marshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().post(uri, data, reqSetting, marshaller);
    }

    public static <T> Future<HttpResponse> rawPut(String clientName, ActorSystem system, String uri, Optional<T> data) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().put(uri, data);
    }

    public static <T> Future<HttpResponse> rawPut(String clientName, ActorSystem system, String uri, Optional<T> data,
                                                  RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().put(uri, data, reqSetting);
    }

    public static <T> Future<HttpResponse> rawPut(String clientName, ActorSystem system, String uri, Optional<T> data,
                                                  Marshaller<T> marshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().put(uri, data, marshaller);
    }

    public static <T> Future<HttpResponse> rawPut(String clientName, ActorSystem system, String uri, Optional<T> data,
                                                  RequestSettings reqSetting, Marshaller<T> marshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().put(uri, data, reqSetting, marshaller);
    }

    public static <T> Future<T> get(String clientName, ActorSystem system, String uri, Class<T> type) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.get(uri, type);
    }

    public static <T> Future<T> get(String clientName, ActorSystem system, String uri, Class<T> type,
                                    RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.get(uri, reqSetting, type);
    }

    public static <T> Future<T> get(String clientName, ActorSystem system, String uri,
                                    Deserializer<HttpResponse, T> unmarshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.get(uri, unmarshaller);
    }

    public static <T> Future<T> get(String clientName, ActorSystem system, String uri,
                                    Deserializer<HttpResponse, T> unmarshaller, RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.get(uri, reqSetting, unmarshaller);
    }

    public static Future<HttpResponse> rawHead(String clientName, ActorSystem system, String uri) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().head(uri);
    }

    public static Future<HttpResponse> rawHead(String clientName, ActorSystem system, String uri,
                                               RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().head(uri, reqSetting);
    }

    public static Future<HttpResponse> rawOptions(String clientName, ActorSystem system, String uri) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().options(uri);
    }

    public static Future<HttpResponse> rawOptions(String clientName, ActorSystem system, String uri,
                                                  RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().options(uri, reqSetting);
    }

    public static <T> Future<T> options(String clientName, ActorSystem system, String uri, Class<T> type) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.options(uri, type);
    }

    public static <T> Future<T> options(String clientName, ActorSystem system, String uri, Class<T> type,
                                        RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.options(uri, reqSetting, type);
    }

    public static <T> Future<T> options(String clientName, ActorSystem system, String uri,
                                        Deserializer<HttpResponse, T> unmarshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.options(uri, unmarshaller);
    }

    public static <T> Future<T> options(String clientName, ActorSystem system, String uri,
                                        Deserializer<HttpResponse, T> unmarshaller, RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.options(uri, reqSetting, unmarshaller);
    }

    public static Future<HttpResponse> rawDelete(String clientName, ActorSystem system, String uri) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().delete(uri);
    }

    public static Future<HttpResponse> rawDelete(String clientName, ActorSystem system, String uri,
                                                 RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.raw().delete(uri, reqSetting);
    }

    public static <T> Future<T> delete(String clientName, ActorSystem system, String uri, Class<T> type) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.delete(uri, type);
    }

    public static <T> Future<T> delete(String clientName, ActorSystem system, String uri, Class<T> type,
                                       RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.delete(uri, reqSetting, type);
    }

    public static <T> Future<T> delete(String clientName, ActorSystem system, String uri,
                                       Deserializer<HttpResponse, T> unmarshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.delete(uri, unmarshaller);
    }

    public static <T> Future<T> delete(String clientName, ActorSystem system, String uri,
                                       Deserializer<HttpResponse, T> unmarshaller, RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.delete(uri, reqSetting, unmarshaller);
    }

    public static <T, R> Future<R> post(String clientName, ActorSystem system, String uri, Optional<T> data,
                                        Class<R> respType) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.post(uri, data, respType);
    }

    public static <T, R> Future<R> post(String clientName, ActorSystem system, String uri, Optional<T> data,
                                        Class<R> respType, RequestSettings reqSetting) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.post(uri, data, reqSetting, respType);
    }

    public static <T, R> Future<R> post(String clientName, ActorSystem system, String uri, Optional<T> data,
                                        Marshaller<T> marshaller, Deserializer<HttpResponse, R> unmarshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.post(uri, data, marshaller, unmarshaller);
    }

    public static <T, R> Future<R> post(String clientName, ActorSystem system, String uri, Optional<T> data,
                                        Marshaller<T> marshaller, Deserializer<HttpResponse, R> unmarshaller,
                                        RequestSettings reqSettings) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.post(uri, data, reqSettings, marshaller, unmarshaller);
    }

    public static <T, R> Future<R> put(String clientName, ActorSystem system, String uri, Optional<T> data,
                                       Class<R> respType) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.put(uri, data, respType);
    }

    public static <T, R> Future<R> put(String clientName, ActorSystem system, String uri, Optional<T> data,
                                       Class<R> respType, RequestSettings reqSettings) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.put(uri, data, reqSettings, respType);
    }

    public static <T, R> Future<R> put(String clientName, ActorSystem system, String uri, Optional<T> data,
                                       Marshaller<T> marshaller, Deserializer<HttpResponse, R> unmarshaller) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.put(uri, data, marshaller, unmarshaller);
    }

    public static <T, R> Future<R> put(String clientName, ActorSystem system, String uri, Optional<T> data,
                                       Marshaller<T> marshaller, Deserializer<HttpResponse, R> unmarshaller,
                                       RequestSettings reqSettings) {
        HttpClient client = HttpClientFactory.get(clientName, system);
        return client.put(uri, data, reqSettings, marshaller, unmarshaller);
    }
    */
}
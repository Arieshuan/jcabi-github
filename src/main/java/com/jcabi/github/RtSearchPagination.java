/**
 * Copyright (c) 2013-2014, JCabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.RequestURI;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Github search pagination.
 *
 * @author Alexander Sinyagin (sinyagin.alexander@gmail.com)
 * @version $Id$
 * @param <T> Type of iterable objects
 */
@Immutable
@EqualsAndHashCode
@SuppressWarnings("PMD.TooManyMethods")
final class RtSearchPagination<T> implements Iterable<T> {

    /**
     * Search request.
     */
    private final transient Request request;

    /**
     * Pagination mapping.
     */
    private final transient RtPagination.Mapping<T, JsonObject> mapping;

    /**
     * Ctor.
     * @param req RESTful API entry point
     * @param path Search path
     * @param keywords Search keywords
     * @param sort Sort field
     * @param order Sort order
     * @param mppng Pagination mapping
     * @checkstyle ParameterNumber (4 lines)
     */
    RtSearchPagination(final Request req, final String path,
        final String keywords, final String sort, final String order,
        final RtPagination.Mapping<T, JsonObject> mppng) {
        this.request = req.uri().path(path)
            .queryParam("q", keywords)
            .queryParam("sort", sort)
            .queryParam("order", order)
            .back();
        this.mapping = mppng;
    }

    @Override
    public Iterator<T> iterator() {
        return new RtPagination<T>(
            new RtSearchPagination.SearchRequest(this.request), this.mapping
        ).iterator();
    }

    /**
     * Request which hides everything but items.
     */
    @SuppressWarnings({ "PMD.TooManyMethods", "PMD.CyclomaticComplexity" })
    private static final class SearchRequest implements Request {
        /**
         * Inner request.
         */
        private final transient Request request;
        /**
         * Ctor.
         * @param req Request to wrap
         */
        SearchRequest(@NotNull(message = "request can't be NULL")
            final Request req) {
            this.request = req;
        }
        @Override
        public RequestURI uri() {
            return this.request.uri();
        }
        @Override
        public RequestBody body() {
            return this.request.body();
        }
        @Override
        public Request header(@NotNull(message = "header name can't be NULL")
            final String name, @NotNull(message = "header value can't be NULL")
            final Object value) {
            return this.request.header(name, value);
        }
        @Override
        public Request reset(@NotNull(message = "header name can't be NULL")
            final String name) {
            return this.request.reset(name);
        }
        @Override
        public Request method(@NotNull(message = "method can't be NULL")
            final String method) {
            return this.request.method(method);
        }

        /**
         * Hide everything from the body but items.
         * @return Response
         * @throws IOException If any I/O problem occurs
         */
        @Override
        public Response fetch() throws IOException {
            final Response response = this.request.fetch();
            // @checkstyle AnonInnerLength (44 lines)
            return new Response() {
                @Override
                public Request back() {
                    return response.back();
                }
                @Override
                public int status() {
                    return response.status();
                }
                @Override
                public String reason() {
                    return response.reason();
                }
                @Override
                public Map<String, List<String>> headers() {
                    return response.headers();
                }
                @Override
                public String body() {
                    return Json.createReader(new StringReader(response.body()))
                        .readObject().getJsonArray("items").toString();
                }
                @Override
                public byte[] binary() {
                    return response.binary();
                }
                // @checkstyle MethodName (3 lines)
                @Override
                @SuppressWarnings("PMD.ShortMethodName")
                public <T> T as(final Class<T> type) {
                    try {
                        return type.getDeclaredConstructor(Response.class)
                            .newInstance(this);
                    } catch (final InstantiationException ex) {
                        throw new IllegalStateException(ex);
                    } catch (final IllegalAccessException ex) {
                        throw new IllegalStateException(ex);
                    } catch (final InvocationTargetException ex) {
                        throw new IllegalStateException(ex);
                    } catch (final NoSuchMethodException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            };
        }
        @Override
        public <T extends Wire> Request through(final Class<T> type,
            final Object... args) {
            return this.request.through(type, args);
        }
    }

}

/*
 * Copyright The OpenZipkin Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package brave.jersey.server.jakarta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.jupiter.api.Test;

import brave.jersey.server.jakarta.TracingApplicationEventListener.ContainerRequestWrapper;

public class ContainerRequestWrapperTest {
  ContainerRequest request = mock(ContainerRequest.class);

  @Test void path_prefixesSlashWhenMissing() {
    when(request.getPath(false)).thenReturn("bar");

    assertThat(new ContainerRequestWrapper(request).path())
      .isEqualTo("/bar");
  }

  @Test void url_derivedFromExtendedUriInfo() {
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(request.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getRequestUri()).thenReturn(URI.create("http://foo:8080/bar?hello=world"));

    assertThat(new ContainerRequestWrapper(request).url())
      .isEqualTo("http://foo:8080/bar?hello=world");
  }
}

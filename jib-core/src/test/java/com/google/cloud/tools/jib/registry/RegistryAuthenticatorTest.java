/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.registry;

import com.google.cloud.tools.jib.JibLogger;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link RegistryAuthenticator}. */
@RunWith(MockitoJUnitRunner.class)
public class RegistryAuthenticatorTest {

  @Mock private JibLogger logger;

  private final RegistryEndpointRequestProperties registryEndpointRequestProperties =
      new RegistryEndpointRequestProperties("someserver", "someimage");

  @Test
  public void testFromAuthenticationMethod_bearer()
      throws MalformedURLException, RegistryAuthenticationFailedException {
    RegistryAuthenticator registryAuthenticator =
        RegistryAuthenticator.fromAuthenticationMethod(
            logger,
            "Bearer realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            registryEndpointRequestProperties);
    Assert.assertEquals(
        new URL("https://somerealm?service=someservice&scope=repository:someimage:scope"),
        registryAuthenticator.getAuthenticationUrl("scope"));

    registryAuthenticator =
        RegistryAuthenticator.fromAuthenticationMethod(
            logger,
            "bEaReR realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            registryEndpointRequestProperties);
    Assert.assertEquals(
        new URL("https://somerealm?service=someservice&scope=repository:someimage:scope"),
        registryAuthenticator.getAuthenticationUrl("scope"));
  }

  @Test
  public void testFromAuthenticationMethod_basic() throws RegistryAuthenticationFailedException {
    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            logger,
            "Basic realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            registryEndpointRequestProperties));

    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            logger,
            "BASIC realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            registryEndpointRequestProperties));

    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            logger,
            "bASIC realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            registryEndpointRequestProperties));
  }

  @Test
  public void testFromAuthenticationMethod_noBearer() {
    try {
      RegistryAuthenticator.fromAuthenticationMethod(
          logger,
          "realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
          registryEndpointRequestProperties);
      Assert.fail("Authentication method without 'Bearer ' or 'Basic ' should fail");

    } catch (RegistryAuthenticationFailedException ex) {
      Assert.assertEquals(
          "Failed to authenticate with the registry because: 'Bearer' was not found in the 'WWW-Authenticate' header, tried to parse: realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
          ex.getMessage());
    }
  }

  @Test
  public void testFromAuthenticationMethod_noRealm() {
    try {
      RegistryAuthenticator.fromAuthenticationMethod(
          logger, "Bearer scope=\"somescope\"", registryEndpointRequestProperties);
      Assert.fail("Authentication method without 'realm' should fail");

    } catch (RegistryAuthenticationFailedException ex) {
      Assert.assertEquals(
          "Failed to authenticate with the registry because: 'realm' was not found in the 'WWW-Authenticate' header, tried to parse: Bearer scope=\"somescope\"",
          ex.getMessage());
    }
  }

  @Test
  public void testFromAuthenticationMethod_noService()
      throws MalformedURLException, RegistryAuthenticationFailedException {
    RegistryAuthenticator registryAuthenticator =
        RegistryAuthenticator.fromAuthenticationMethod(
            logger, "Bearer realm=\"https://somerealm\"", registryEndpointRequestProperties);

    Assert.assertEquals(
        new URL("https://somerealm?service=someserver&scope=repository:someimage:scope"),
        registryAuthenticator.getAuthenticationUrl("scope"));
  }
}

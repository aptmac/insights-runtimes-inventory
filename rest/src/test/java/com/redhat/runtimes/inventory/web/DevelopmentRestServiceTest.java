/* Copyright (C) Red Hat 2023 */
package com.redhat.runtimes.inventory.web;

import static com.redhat.runtimes.inventory.events.TestUtils.clearTables;
import static com.redhat.runtimes.inventory.events.TestUtils.createRHIdentityHeader;
import static com.redhat.runtimes.inventory.events.TestUtils.encodeRHIdentityInfo;
import static com.redhat.runtimes.inventory.events.TestUtils.getJvmInstanceFromJsonFile;
import static com.redhat.runtimes.inventory.events.TestUtils.getJvmInstanceFromZipJsonFile;

import com.redhat.runtimes.inventory.models.JvmInstance;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
@Tag("mock-rest-service")
public class DevelopmentRestServiceTest {

  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  public void setup()
      throws IOException { // Setup two jvm instances with the same hostname, combining for 12 jar
    // hashes
    entityManager.persist(getJvmInstanceFromZipJsonFile("jdk8_MWTELE-66.gz"));
    JvmInstance modifiedInstance = getJvmInstanceFromJsonFile("test17.json");
    // set the hostname to match the previous instance
    modifiedInstance.setHostname("fedora");
    entityManager.persist(modifiedInstance);
  }

  @AfterEach
  @Transactional
  public void tearDown() {
    clearTables(entityManager);
  }

  @Test
  public void main() {
    Header identityHeader =
        createRHIdentityHeader(encodeRHIdentityInfo("accountId", "orgId", "user"));
    String text =
        """
      ***
      Mock rest service for local frontend development
      ***

      Mock identity header: %s

      Sample requests:
        curl -X GET http://127.0.0.1:9087/api/runtimes-inventory-service/v1/instance-ids/\\?hostname=fedora -H "x-rh-identity:%s"

        curl -X GET http://127.0.0.1:9087/api/runtimes-inventory-service/v1/instance/\\?jvmInstanceId=$JVM_INSTANCE_ID -H "x-rh-identity:%s"

        curl -X GET http://127.0.0.1:9087/api/runtimes-inventory-service/v1/instances/\\?hostname=fedora -H "x-rh-identity:%s"
      
      This test function is stuck in an infinite loop. When finished testing, interrupt this application.
      """
            .formatted(
                identityHeader.getValue(),
                identityHeader.getValue(),
                identityHeader.getValue(),
                identityHeader.getValue());
    System.out.println(text);
    for (;;) {}
  }
}

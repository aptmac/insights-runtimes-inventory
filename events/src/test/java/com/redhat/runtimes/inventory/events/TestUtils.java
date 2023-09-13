/* Copyright (C) Red Hat 2023 */
package com.redhat.runtimes.inventory.events;

import static com.redhat.runtimes.inventory.models.Constants.X_RH_IDENTITY_HEADER;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.runtimes.inventory.models.EapInstance;
import com.redhat.runtimes.inventory.models.InsightsMessage;
import com.redhat.runtimes.inventory.models.JvmInstance;
import io.restassured.http.Header;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;

public final class TestUtils {
  private TestUtils() {}

  public static String readFromResources(String fName) throws IOException {
    return new String(readBytesFromResources(fName));
  }

  public static byte[] readBytesFromResources(String fName) throws IOException {
    try (final InputStream is =
            ArchiveAnnouncementParserTest.class.getClassLoader().getResourceAsStream(fName);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      for (; ; ) {
        int nread = is.read(buffer);
        if (nread <= 0) {
          break;
        }
        baos.write(buffer, 0, nread);
      }
      return baos.toByteArray();
    }
  }

  public static JvmInstance getJvmInstanceFromJsonFile(String filename) throws IOException {
    String json = TestUtils.readFromResources(filename);
    InsightsMessage message = Utils.jvmInstanceOf(setupArchiveAnnouncement(), json);
    assertTrue(message instanceof JvmInstance);
    JvmInstance instance = (JvmInstance) message;
    return instance;
  }

  public static JvmInstance getJvmInstanceFromZipJsonFile(String filename) throws IOException {
    byte[] buffy = TestUtils.readBytesFromResources(filename);
    String json = EventConsumer.unzipJson(buffy);
    InsightsMessage message = Utils.jvmInstanceOf(setupArchiveAnnouncement(), json);
    assertTrue(message instanceof JvmInstance);
    JvmInstance instance = (JvmInstance) message;
    return instance;
  }

  public static EapInstance getEapInstanceFromJsonFile(String filename) throws IOException {
    String json = TestUtils.readFromResources(filename);
    InsightsMessage message = Utils.eapInstanceOf(TestUtils.setupArchiveAnnouncement(), json);
    assertTrue(message instanceof EapInstance);
    EapInstance instance = (EapInstance) message;
    return instance;
  }

  public static Long entity_count(EntityManager entityManager, String entity) {
    // I don't know why, but but hibernate throws a ParsingException
    // when I try a named or positional query parameter
    return entityManager
        .createQuery("SELECT COUNT (*) FROM " + entity, Long.class)
        .getSingleResult();
  }

  public static Long table_count(EntityManager entityManager, String table) {
    // I don't know why, but but hibernate throws a ParsingException
    // when I try a named or positional query parameter
    return (Long)
        entityManager
            .createNativeQuery("SELECT COUNT (*) FROM " + table, Long.class)
            .getSingleResult();
  }

  public static void clearTables(EntityManager entityManager) {
    // Order is important here
    entityManager.createNativeQuery("DELETE FROM jvm_instance_jar_hash").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_instance_module_jar_hash").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_deployment_archive_jar_hash").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM jar_hash").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM jvm_instance").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_deployment").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_configuration_eap_extension").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_configuration").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_instance").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_configuration_deployments").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_configuration_subsystems").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_extension").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM eap_extension_subsystems").executeUpdate();
  }

  public static Header createRHIdentityHeader(String encodedIdentityHeader) {
    return new Header(X_RH_IDENTITY_HEADER, encodedIdentityHeader);
  }

  public static String encode(String value) {
    return new String(Base64.getEncoder().encode(value.getBytes()));
  }

  public static String encodeRHIdentityInfo(String accountNumber, String orgId, String username) {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode user = mapper.createObjectNode();
    user.put("username", username);

    ObjectNode identity = mapper.createObjectNode();
    identity.put("account_number", accountNumber);
    identity.put("org_id", orgId);
    identity.set("user", user);
    identity.put("type", "User");

    ObjectNode head = mapper.createObjectNode();
    head.set("identity", identity);

    return encode(head.toString());
  }

  private static ArchiveAnnouncement setupArchiveAnnouncement() {
    ArchiveAnnouncement announcement = new ArchiveAnnouncement();
    announcement.setAccountId("accountId");
    announcement.setOrgId("orgId");
    announcement.setTimestamp(Instant.now());
    return announcement;
  }
}

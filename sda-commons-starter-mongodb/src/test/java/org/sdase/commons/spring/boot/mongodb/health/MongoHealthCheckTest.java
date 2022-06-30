package org.sdase.commons.spring.boot.mongodb.health;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoHealthCheckTest {
  private static MongoDatabase mockdb;
  private Health.Builder builder = new Health.Builder();

  @BeforeAll
  public static void setUp(){
    mockdb = mock(MongoDatabase.class, Mockito.RETURNS_DEEP_STUBS);
  }

  @Test
  public void shouldUHealthy() throws Exception {
    //Given
    when(mockdb.runCommand(Mockito.any())).thenReturn(new Document("ok", Double.valueOf("1.0")));
    //then
    assertThat(new MongoHealthCheck(mockdb).health().getStatus().equals(builder.up()));
  }

  @Test
  public void shouldBeUnHealthy() throws Exception {
    //Given
    when(mockdb.runCommand(Mockito.any())).thenReturn(new Document("ok", Double.valueOf("1.0")));
    //then
    assertThat(new MongoHealthCheck(mockdb).health().getStatus().equals(builder.down()));
  }

}
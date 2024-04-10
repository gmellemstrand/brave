/*
 * Copyright The OpenZipkin Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package brave.messaging;

import brave.Tracing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

// copy of tests in CurrentTracingTest as same pattern is used
public class CurrentMessagingTracingTest {
  Tracing tracing = mock(Tracing.class);

  @BeforeEach void reset() {
    MessagingTracing.CURRENT.set(null);
  }

  @AfterEach void close() {
    MessagingTracing current = MessagingTracing.current();
    if (current != null) current.close();
  }

  @Test void defaultsToNull() {
    assertThat(MessagingTracing.current()).isNull();
  }

  @Test void autoRegisters() {
    MessagingTracing current = MessagingTracing.create(tracing);

    assertThat(MessagingTracing.current())
      .isSameAs(current);
  }

  @Test void setsNotCurrentOnClose() {
    autoRegisters();

    MessagingTracing.current().close();

    assertThat(MessagingTracing.current()).isNull();
  }

  @Test void canSetCurrentAgain() {
    setsNotCurrentOnClose();

    autoRegisters();
  }

  @Test void onlyRegistersOnce() throws InterruptedException {
    final MessagingTracing[] threadValues =
      new MessagingTracing[10]; // array ref for thread-safe setting

    List<Thread> getOrSet = new ArrayList<>(20);

    for (int i = 0; i < 10; i++) {
      final int index = i;
      getOrSet.add(new Thread(() -> threadValues[index] = MessagingTracing.current()));
    }
    for (int i = 10; i < 20; i++) {
      getOrSet.add(new Thread(() -> MessagingTracing.create(tracing)));
    }

    // make it less predictable
    Collections.shuffle(getOrSet);

    // start the races
    getOrSet.forEach(Thread::start);
    for (Thread thread : getOrSet) {
      thread.join();
    }

    Set<MessagingTracing> messagingTracings = new LinkedHashSet<>(Arrays.asList(threadValues));
    messagingTracings.remove(null);
    // depending on race, we should have either one instance or none
    assertThat(messagingTracings.isEmpty() || messagingTracings.size() == 1)
      .isTrue();
  }
}

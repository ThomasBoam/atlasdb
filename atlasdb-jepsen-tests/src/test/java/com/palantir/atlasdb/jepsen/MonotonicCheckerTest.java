/**
 * Copyright 2016 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.jepsen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.palantir.atlasdb.jepsen.events.Event;
import com.palantir.atlasdb.jepsen.events.ImmutableOkEvent;

public class MonotonicCheckerTest {
    private static final Long ZERO_TIME = 0L;
    private static final int PROCESS_0 = 0;
    private static final int PROCESS_1 = 1;

    @Test
    public void shouldPassOnNoEvents() {
        MonotonicChecker monotonicChecker = runMonotonicChecker();

        assertThat(monotonicChecker.valid()).isTrue();
        assertThat(monotonicChecker.errors()).isEmpty();
    }

    @Test
    public void shouldFailOnDecreasingValueEvents() {
        Event event1 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(1L)
                .build();
        Event event2 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(0L)
                .build();

        MonotonicChecker monotonicChecker = runMonotonicChecker(event1, event2);

        assertThat(monotonicChecker.valid()).isFalse();
        assertThat(monotonicChecker.errors()).containsExactly(event1, event2);
    }

    @Test
    public void shouldFailOnEqualEntries() {
        Event event1 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(0L)
                .build();
        Event event2 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(0L)
                .build();

        MonotonicChecker monotonicChecker = runMonotonicChecker(event1, event2);

        assertThat(monotonicChecker.valid()).isFalse();
        assertThat(monotonicChecker.errors()).containsExactly(event1, event2);
    }

    @Test
    public void shouldPassOnTwoProcessesOverlapping() {
        Event event1 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(1L)
                .build();
        Event event2 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_1)
                .value(2L)
                .build();
        Event event3 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_0)
                .value(4L)
                .build();
        Event event4 = ImmutableOkEvent.builder()
                .time(ZERO_TIME)
                .process(PROCESS_1)
                .value(3L)
                .build();

        MonotonicChecker monotonicChecker = runMonotonicChecker(event1, event2, event3, event4);

        assertThat(monotonicChecker.valid()).isTrue();
        assertThat(monotonicChecker.errors()).isEmpty();
    }

    private static MonotonicChecker runMonotonicChecker(Event... events) {
        MonotonicChecker monotonicChecker = new MonotonicChecker();
        Arrays.asList(events).forEach(event -> event.accept(monotonicChecker));
        return monotonicChecker;
    }
}

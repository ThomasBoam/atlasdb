/**
 * Copyright 2015 Palantir Technologies
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
package com.palantir.paxos;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.palantir.common.concurrent.PTExecutors;
import com.palantir.leader.LeaderElectionService;
import com.palantir.leader.PaxosLeaderElectionService;
import com.palantir.leader.PingableLeader;
import com.palantir.leader.proxy.SimulatingFailingServerProxy;
import com.palantir.leader.proxy.ToggleableExceptionProxy;

// TODO rename this and other PaxosConsensus*Test classes to LeadershipConsensus*Test
public final class PaxosConsensusTestUtils {

    private PaxosConsensusTestUtils() {
    }

    private static final long SERVER_DELAY_TIME_MS = 0;
    private static final String LOG_DIR = "testlogs/";
    private static final String LEARNER_DIR_PREFIX = LOG_DIR + "learner/";
    private static final String ACCEPTOR_DIR_PREFIX = LOG_DIR + "acceptor/";

    public static PaxosTestState setup(int numLeaders,
                                       int quorumSize) {
        List<LeaderElectionService> leaders = Lists.newArrayList();
        List<PaxosAcceptor> acceptors = Lists.newArrayList();
        List<OrderedPaxosLearner> learners = Lists.newArrayList();
        List<AtomicBoolean> failureToggles = Lists.newArrayList();
        ExecutorService executor = PTExecutors.newCachedThreadPool();

        RuntimeException e = new RuntimeException("mock server failure");
        for (int i = 0; i < numLeaders; i++) {
            failureToggles.add(new AtomicBoolean(false));

            // TODO factory method?
            OrderedPaxosLearner learner = OrderedPaxosLearner.newLearner(PaxosLearnerImpl.newLearner(getLearnerLogDir(i)));
            learners.add(ToggleableExceptionProxy.newProxyInstance(
                    OrderedPaxosLearner.class,
                    learner,
                    failureToggles.get(i),
                    e));

            PaxosAcceptor acceptor = PaxosAcceptorImpl.newAcceptor(getAcceptorLogDir(i));
            acceptors.add(ToggleableExceptionProxy.newProxyInstance(
                    PaxosAcceptor.class,
                    acceptor,
                    failureToggles.get(i),
                    e));
        }

        for (int i = 0; i < numLeaders; i++) {
            PaxosProposer proposer = PaxosProposerImpl.newProposer(
                    learners.get(i),
                    ImmutableList.<PaxosAcceptor> copyOf(acceptors),
                    ImmutableList.<PaxosLearner> copyOf(learners),
                    quorumSize,
                    executor);
            PaxosLeaderElectionService leader = new PaxosLeaderElectionService(
                    proposer,
                    learners.get(i),
                    ImmutableMap.<PingableLeader, HostAndPort>of(),
                    ImmutableList.<PaxosAcceptor> copyOf(acceptors),
                    ImmutableList.<OrderedPaxosLearner> copyOf(learners),
                    executor,
                    0L, 0L, 0L);
            leaders.add(SimulatingFailingServerProxy.newProxyInstance(
                    LeaderElectionService.class,
                    leader,
                    SERVER_DELAY_TIME_MS,
                    failureToggles.get(i)));
        }

        return new PaxosTestState(leaders, acceptors, learners, failureToggles, executor);
    }

    public static void teardown(PaxosTestState state) throws Exception {
        try {
            ExecutorService executor = state.getExecutor();
            executor.shutdownNow();
            boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated) {
                throw new IllegalStateException("Some threads are still hanging around! Can't proceed or they might corrupt future tests.");
            }
        } finally {
            FileUtils.deleteDirectory(new File(LOG_DIR));
        }
    }

    public static String getLearnerLogDir(int i) {
        return LEARNER_DIR_PREFIX + i;
    }

    public static String getAcceptorLogDir(int i) {
        return ACCEPTOR_DIR_PREFIX + i;
    }
}

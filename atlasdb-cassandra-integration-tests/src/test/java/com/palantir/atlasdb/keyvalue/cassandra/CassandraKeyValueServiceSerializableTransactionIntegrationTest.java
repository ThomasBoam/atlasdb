/**
 * Copyright 2015 Palantir Technologies
 * <p>
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.cassandra;

import com.palantir.atlasdb.cassandra.CassandraKeyValueServiceConfigManager;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.transaction.impl.AbstractSerializableTransactionTest;

public class CassandraKeyValueServiceSerializableTransactionIntegrationTest extends
        AbstractSerializableTransactionTest {

    @Override
    protected KeyValueService getKeyValueService() {
        return CassandraKeyValueService.create(
                CassandraKeyValueServiceConfigManager.createSimpleManager(CassandraTestSuiteUtils.CASSANDRA_KVS_CONFIG), CassandraTestSuiteUtils.LEADER_CONFIG);
    }

    @Override
    protected boolean supportsReverse() {
        return false;
    }

}

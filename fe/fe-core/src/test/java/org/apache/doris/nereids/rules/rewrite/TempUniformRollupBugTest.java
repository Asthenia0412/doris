// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.rules.rewrite;

import org.apache.doris.nereids.sqltest.SqlTestBase;
import org.apache.doris.nereids.util.PlanChecker;

import org.junit.jupiter.api.Test;

public class TempUniformRollupBugTest extends SqlTestBase {

    @Override
    protected void runBeforeAll() throws Exception {
        super.runBeforeAll();
        createTables(
                "CREATE TABLE uniform_t (\n"
                        + "    dt  DATE NOT NULL,\n"
                        + "    grp VARCHAR(64) NOT NULL,\n"
                        + "    id  VARCHAR(64) NOT NULL,\n"
                        + "    cnt BIGINT SUM NOT NULL\n"
                        + ") AGGREGATE KEY(dt, grp, id)\n"
                        + "DISTRIBUTED BY HASH(id) BUCKETS 8\n"
                        + "PROPERTIES (\"replication_num\" = \"1\")");
    }

    @Test
    void testQuery1Shape() {
        String sql = "select dt, id, sum(cnt) from uniform_t where grp = 'g1' and id = 'id1' group by dt, id";
        PlanChecker.from(connectContext)
                .analyze(sql)
                .rewrite()
                .printlnTree();
    }

    @Test
    void testQuery2Shape() {
        String sql = "select dt, sum(cnt) from uniform_t where grp = 'g1' and id = 'id1' group by dt";
        PlanChecker.from(connectContext)
                .analyze(sql)
                .rewrite()
                .printlnTree();
    }
}

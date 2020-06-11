/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iceberg.mr.mapred;

import java.util.List;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;

public class SystemTableUtil {

  static final String VIRTUAL_COLUMN_NAME = "iceberg.hive.snapshot.virtual.column.name";

  private static final String DEFAULT_SNAPSHOT_ID_COLUMN_NAME = "snapshot__id";

  private SystemTableUtil() {}

  protected static Schema schemaWithSnapshotIdVirtualColumn(Schema schema, String columnName) {
    List<Types.NestedField> columns = Lists.newArrayList(schema.columns());
    columns.add(Types.NestedField.optional(Integer.MAX_VALUE, columnName, Types.LongType.get()));
    return new Schema(columns);
  }

  protected static Record recordWithSnapshotIdVirtualColumn(Record record, long snapshotId, Schema oldSchema,
                                                            String virtualColumnName) {
    Schema newSchema = schemaWithSnapshotIdVirtualColumn(oldSchema, virtualColumnName);
    Record newRecord = GenericRecord.create(newSchema);
    for (int i = 0; i < oldSchema.columns().size(); i++) {
      newRecord.set(i, record.get(i));
    }
    newRecord.setField(virtualColumnName, snapshotId);
    return newRecord;
  }

  protected static String snapshotIdVirtualColumnName(Configuration conf) {
    String virtualColumnName = conf.get(VIRTUAL_COLUMN_NAME);
    if (virtualColumnName == null) {
      return DEFAULT_SNAPSHOT_ID_COLUMN_NAME;
    } else {
      return virtualColumnName;
    }
  }

  protected static String snapshotIdVirtualColumnName(Properties properties) {
    String virtualColumnName = properties.getProperty(VIRTUAL_COLUMN_NAME);
    if (virtualColumnName == null) {
      return DEFAULT_SNAPSHOT_ID_COLUMN_NAME;
    } else {
      return virtualColumnName;
    }
  }

}

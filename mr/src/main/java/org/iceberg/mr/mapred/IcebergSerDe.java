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

package org.iceberg.mr.mapred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nullable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.Writable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.types.Types;

public class IcebergSerDe extends AbstractSerDe {

  private Schema schema;
  private TableMetadata metadata;
  private ObjectInspector inspector;
  private List<String> columnNames;
  private List<TypeInfo> columnTypes;

  @Override
  public void initialize(@Nullable Configuration configuration, Properties properties) throws SerDeException {
    //TODO Add methods to dynamically find most recent metadata
    String tableDir = properties.getProperty("location") + "/metadata/v2.metadata.json";
    this.metadata = TableMetadataParser.read(new HadoopFileIO(configuration), tableDir);
    this.schema = metadata.schema();

    try {
      this.inspector = new IcebergObjectInspectorGenerator().createObjectInspector(schema);
    } catch (Exception e) {
      throw new SerDeException(e);
    }
  }

  @Override
  public Class<? extends Writable> getSerializedClass() {
    return null;
  }

  @Override
  public Writable serialize(Object o, ObjectInspector objectInspector) throws SerDeException {
    return null;
  }

  @Override
  public SerDeStats getSerDeStats() {
    return null;
  }

  @Override
  public Object deserialize(Writable writable) throws SerDeException {
    IcebergWritable icebergWritable = (IcebergWritable) writable;
    List<Types.NestedField> fields = icebergWritable.getSchema().columns();
    List<Object> row = new ArrayList<>();
    for (Types.NestedField field : fields) {
      Object obj = ((IcebergWritable) writable).getRecord().getField(field.name());
      row.add(obj);
    }
    return Collections.unmodifiableList(row);
  }

  @Override
  public ObjectInspector getObjectInspector() throws SerDeException {
    return inspector;
  }
}
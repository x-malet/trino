/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.hive.s3select.json;

import com.amazonaws.services.s3.model.ExpressionType;
import com.amazonaws.services.s3.model.InputSerialization;
import com.amazonaws.services.s3.model.JSONInput;
import com.amazonaws.services.s3.model.JSONOutput;
import com.amazonaws.services.s3.model.JSONType;
import com.amazonaws.services.s3.model.OutputSerialization;
import com.amazonaws.services.s3.model.SelectObjectContentRequest;
import io.trino.plugin.hive.s3.TrinoS3FileSystem;
import io.trino.plugin.hive.s3select.S3SelectLineRecordReader;
import io.trino.plugin.hive.s3select.TrinoS3ClientFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.net.URI;
import java.util.Properties;

public class S3SelectJsonRecordReader
        extends S3SelectLineRecordReader
{
    public S3SelectJsonRecordReader(Configuration configuration,
                             Path path,
                             long start,
                             long length,
                             Properties schema,
                             String ionSqlQuery,
                             TrinoS3ClientFactory s3ClientFactory)
    {
        super(configuration, path, start, length, schema, ionSqlQuery, s3ClientFactory);
    }

    @Override
    public SelectObjectContentRequest buildSelectObjectRequest(Properties schema, String query, Path path)
    {
        SelectObjectContentRequest selectObjectRequest = new SelectObjectContentRequest();
        URI uri = path.toUri();
        selectObjectRequest.setBucketName(TrinoS3FileSystem.extractBucketName(uri));
        selectObjectRequest.setKey(TrinoS3FileSystem.keyFromPath(path));
        selectObjectRequest.setExpression(query);
        selectObjectRequest.setExpressionType(ExpressionType.SQL);

        // JSONType.LINES is the only JSON format supported by the Hive JsonSerDe.
        JSONInput selectObjectJSONInputSerialization = new JSONInput();
        selectObjectJSONInputSerialization.setType(JSONType.LINES);

        InputSerialization selectObjectInputSerialization = new InputSerialization();
        selectObjectInputSerialization.setCompressionType(getCompressionType(path));
        selectObjectInputSerialization.setJson(selectObjectJSONInputSerialization);
        selectObjectRequest.setInputSerialization(selectObjectInputSerialization);

        OutputSerialization selectObjectOutputSerialization = new OutputSerialization();
        JSONOutput selectObjectJSONOutputSerialization = new JSONOutput();
        selectObjectOutputSerialization.setJson(selectObjectJSONOutputSerialization);
        selectObjectRequest.setOutputSerialization(selectObjectOutputSerialization);

        return selectObjectRequest;
    }
}

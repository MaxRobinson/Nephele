package com.nephele.resources.s3;

/*-
 * #%L
 * NepheleService
 * %%
 * Copyright (C) 2020 - 2022 Max Robinson
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.nephele.service.mirror.ComparisonFileObject;
import com.nephele.service.mirror.S3UploadMirror;
import com.nephele.service.s3.S3Path;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Path("/s3")
public class S3Resource {

    @Inject
    S3Client s3;

    @Inject
    S3UploadMirror s3UploadMirror;

    private final String bucket = "operational-analytics-data-and-reports";

    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String status(){
        ListBucketsResponse res = s3.listBuckets();
        boolean hasBuckets = res.hasBuckets();
        return "Successfully Connected to S3. Buckets? " + hasBuckets;
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm S3UploadFormData formData) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formData.data.transferTo(out);

        java.nio.file.Path p = Paths.get(formData.filePath);
        S3Path s3path = S3Path.fromPath(p.toString());
        if(s3path.getBucket() == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Path could not be translated to a target configured bucket")
                    .build();
        }

        boolean success = s3UploadMirror.upload(p.toString(), new ByteArrayInputStream(out.toByteArray()), out.size());
        if(success){
            return Response.ok().status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }


    @GET
    @Path("download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("objectKey") String objectKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GetObjectRequest getRequest = GetObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(objectKey)
                                        .build();

        GetObjectResponse object = s3.getObject(getRequest, ResponseTransformer.toOutputStream(baos));

        ResponseBuilder response = Response.ok((StreamingOutput) output -> baos.writeTo(output));
        response.header("Content-Disposition", "attachment;filename=" + objectKey);
        response.header("Content-Type", object.contentType());
        return response.build();
    }

    @GET
    @Path("list/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ComparisonFileObject> listFiles(@PathParam("path") String bucket) throws Exception {
        // HEAD S3 objects to get metadata
        return s3UploadMirror.getComparableFiles(bucket);
    }

    @GET
    @Path("object/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getMetadata(@QueryParam("bucket") String bucket, @QueryParam("key") String key) {
        HeadObjectRequest r = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        HeadObjectResponse resp = s3.headObject(r);
        return resp.metadata();
    }

//
//    protected File uploadToTemp(InputStream data) {
//        File tempPath;
//        try {
//            tempPath = File.createTempFile("uploadS3Tmp", ".tmp");
//            Files.copy(data, tempPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//
//        return tempPath;
//    }
}

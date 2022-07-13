# sda-commons-s3-starter

This library provides methods for dealing with the Amazon S3 file storage.

The configuration class contains two beans, namely

- `AmazonS3Client`: Providing an interface for accessing the S3 object storage.
- `S3BucketRepository`: Providing an abstraction for s3 client with simple repository methods.

## Configuration

The following properties are needed for the configuration.

| **Property**             | **Description**                                       | **Example**                                                      | **Env**          |
|--------------------------|-------------------------------------------------------|------------------------------------------------------------------|------------------|
| `s3.bucketName` _string_ | The name of the bucket containing the desired object. | myphotos                                                         | `S3_BUCKET_NAME` |
| `s3.endpoint` _string_   | The endpoint either with or without the protocol      | https://s3.eu-west-1.amazonaws.com or s3.eu-west-1.amazonaws.com | `S3_ENDPOINT`    |
| `s3.region` _string_     | The region to use for SigV4 signing of requests       | eu-west-1                                                        | `S3_REGION`      |
| `s3.secretKey` _string_  | The AWS secret access key                             | s3cret                                                           | `S3_SECRET_KEY`  |
| `s3.accessKey` _string_  | The AWS access key                                    | s3cretAccess                                                     | `S3_ACCESS_KEY`  |


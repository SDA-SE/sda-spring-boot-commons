| **Property**             | **Description**                                       | **Example**                                                      | **Env**          |
|--------------------------|-------------------------------------------------------|------------------------------------------------------------------|------------------|
| `s3.bucketName` _string_ | The name of the bucket containing the desired object. | myphotos                                                         | `S3_BUCKET_NAME` |
| `s3.endpoint` _string_   | The endpoint either with or without the protocol      | https://s3.eu-west-1.amazonaws.com or s3.eu-west-1.amazonaws.com | `S3_ENDPOINT`    |
| `s3.region` _string_     | The region to use for SigV4 signing of requests       | eu-west-1                                                        | `S3_REGION`      |
| `s3.secretKey` _string_  | The AWS secret access key                             | s3cret                                                           | `S3_SECRET_KEY`  |
| `s3.accessKey` _string_  | The AWS access key                                    | s3cretAccess                                                     | `S3_ACCESS_KEY`  |

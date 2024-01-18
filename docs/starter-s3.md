# Starter S3

This library provides features for dealing with the Amazon S3 file storage.

Based on:
  - `io.awspring.cloud:spring-cloud-aws-core`

The configuration class contains two beans, namely:

- `AmazonS3Client`: Providing an interface for accessing the S3 object storage.
- `S3BucketRepository`: Providing an abstraction for s3 client with simple repository methods.

## Configuration

The following properties are needed for the configuration.

--8<-- "doc-snippets/config-starter-s3.md"

## Testing

We recommend to use [Robothy's local-s3](https://github.com/Robothy/local-s3) JUnit 5 extension
for testing.
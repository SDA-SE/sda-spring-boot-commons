
# S3 starter

This library provides methods for dealing with the Amazon S3 file storage.

# Configuration

The configuration class contains 2 beans, namely

1) getAmazonS3Client: This bean is used for the initialisation and creation of 
   the s3Client using environment variables.
 
2) S3BucketRepository: This bean receives the S3Client as parameter via dependency
   injection and performs the different file operations on the S3Cleint.

# Environment variables

The following environment variables are needed for the configuration. All passed as 
string in the property file. 

- s3.accessKey  
- s3.secretKey
- s3.endpoint
- s3.region
- s3.bucketName


They are mapped to  the instance variables in the configuration class as 
follows:

@Value("${s3.secretKey}")
private String secretKey

@Value("${s3.endpoint}")
private String endpoint;

@Value("${s3.region}")
private String region;

@Value("${s3.bucketName}")
private String bucketName;

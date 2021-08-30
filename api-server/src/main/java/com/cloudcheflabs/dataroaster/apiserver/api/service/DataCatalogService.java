package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface DataCatalogService {

    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String s3AccessKey,
                String s3SecretKey,
                String s3Endpoint,
                String storageClass,
                int storageSize);
    void delete(long serviceId, String userName);
}

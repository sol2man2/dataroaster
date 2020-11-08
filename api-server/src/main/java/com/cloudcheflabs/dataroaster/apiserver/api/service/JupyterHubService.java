package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface JupyterHubService {

    void createJupyterHub(long namespaceId,
                          long serviceId,
                          int storage);

    void deleteJupyterHub(long namespaceId, long serviceId);
}
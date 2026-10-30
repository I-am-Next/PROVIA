package com.pm.mediastorage.domain.port.out;

public interface StoragePort {

    void store(StorageObject object);

    StorageContent retrieve(String objectKey);

    void delete(String objectKey);
}
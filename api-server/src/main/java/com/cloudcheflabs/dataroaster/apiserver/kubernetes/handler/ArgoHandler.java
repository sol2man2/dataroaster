package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ArgoHandler {

    private static Logger LOG = LoggerFactory.getLogger(ArgoHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "workflow";
        String serviceName = "argo";

        String tempDirectory = System.getProperty("java.io.tmpdir") + "/" + serviceTypeName + "/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // minio version.
        String version = k8sServices.getVersion();

        String rootPath = "/templates/" + serviceTypeName + "/";
        String versionedPath = serviceName + "-" + version;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, versionedPath, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace,
                                String bucket,
                                String s3Endpoint,
                                boolean insecure,
                                String accessKey,
                                String secretKey) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                bucket,
                s3Endpoint,
                insecure,
                accessKey,
                secretKey);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "argo created...";
    }


    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                "any-bucket",
                "any endpoint",
                false,
                "any access key",
                "any secret key");

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "argo deleted...";
    }

    private static void substitute(String tempDirectory,
                                   String namespace,
                                   String bucket,
                                   String s3Endpoint,
                                   boolean insecure,
                                   String accessKey,
                                   String secretKey) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        kv.put("bucket", bucket);
        kv.put("s3Endpoint", s3Endpoint);
        kv.put("insecure", String.valueOf(insecure));
        kv.put("accessKey", accessKey);
        kv.put("secretKey", secretKey);

        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "install.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}


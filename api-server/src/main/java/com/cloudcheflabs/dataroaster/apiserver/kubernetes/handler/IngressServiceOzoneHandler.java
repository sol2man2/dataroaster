package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IngressServiceOzoneHandler {

    private static Logger LOG = LoggerFactory.getLogger(IngressServiceOzoneHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {

        String serviceTypeName = "ingress";
        String serviceName = "ozone";

        String tempDirectory = System.getProperty("java.io.tmpdir") + "/" + serviceTypeName + "/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);


        String rootPath = "/templates/" + serviceTypeName + "/";

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, serviceName, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig,
                                String namespace,
                                String host) {

        String tempDirectory = moveFiles(kubeconfig);

        String ingressYaml = "ingress-ozone.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("namespace", namespace);
        kv.put("host", host);
        TemplateUtils.toFile(tempDirectory + "/" + ingressYaml, false, kv, tempDirectory + "/" + ingressYaml, false);

        String createSh = "create.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createSh, false, kv, tempDirectory + "/" + createSh, true);
        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "ozone ingress created...";
    }
}

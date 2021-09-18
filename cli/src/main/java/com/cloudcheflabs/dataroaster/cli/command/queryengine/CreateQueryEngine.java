package com.cloudcheflabs.dataroaster.cli.command.queryengine;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Data Catalog.")
public class CreateQueryEngine implements Callable<Integer> {

    @CommandLine.ParentCommand
    private QueryEngine parent;

    @CommandLine.Option(names = {"--s3-bucket"}, description = "S3 Bucket Name", required = true)
    private String s3Bucket;

    @CommandLine.Option(names = {"--s3-access-key"}, description = "S3 Access Key", required = true)
    private String s3AccessKey;

    @CommandLine.Option(names = {"--s3-secret-key"}, description = "S3 Secret Key", required = true)
    private String s3SecretKey;

    @CommandLine.Option(names = {"--s3-endpoint"}, description = "S3 Endpoint", required = true)
    private String s3Endpoint;

    @CommandLine.Option(names = {"--spark-thrift-server-executors"}, description = "Spark Thrift Server Executor Count", required = true)
    private int sparkThriftServerExecutors;

    @CommandLine.Option(names = {"--spark-thrift-server-executor-memory"}, description = "Spark Thrift Server Executor Memory in GB", required = true)
    private int sparkThriftServerExecutorMemory;

    @CommandLine.Option(names = {"--spark-thrift-server-executor-cores"}, description = "Spark Thrift Server Executor Core Count", required = true)
    private int sparkThriftServerExecutorCores;

    @CommandLine.Option(names = {"--spark-thrift-server-driver-memory"}, description = "Spark Thrift Server Driver Memory in GB", required = true)
    private int sparkThriftServerDriverMemory;

    @CommandLine.Option(names = {"--trino-workers"}, description = "Trino Worker Count", required = true)
    private int trinoWorkers;

    @CommandLine.Option(names = {"--trino-server-max-memory"}, description = "Trino Server Max. Memory in GB", required = true)
    private int trinoServerMaxMemory;

    @CommandLine.Option(names = {"--trino-cores"}, description = "Trino Server Core Count", required = true)
    private int trinoCores;

    @CommandLine.Option(names = {"--trino-temp-data-storage"}, description = "Trino Temporary Data Storage in GB", required = true)
    private int trinoTempDataStorage;

    @CommandLine.Option(names = {"--trino-data-storage"}, description = "Trino Data Storage in GiB", required = true)
    private int trinoDataStorage;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show project list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.listProjects(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> projectLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"PROJECT ID", "PROJECT NAME", "PROJECT DESCRIPTION");
        for(Map<String, Object> map : projectLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        String projectId = cnsl.readLine("Select Project : ");
        if(projectId == null) {
            throw new RuntimeException("project id is required!");
        }

        System.out.printf("\n");


        // show cluster list.
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        System.out.printf("\n");

        String clusterId = cnsl.readLine("Select Cluster : ");
        if(clusterId == null) {
            throw new RuntimeException("cluster id is required!");
        }

        System.out.printf("\n");

        // show storage classes.
        ResourceControlDao resourceControlDao = applicationContext.getBean(ResourceControlDao.class);
        restResponse = resourceControlDao.listStorageClasses(configProps, Long.valueOf(clusterId));

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> storageClasses =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        format = "%-20s%-20s%-20s%-20s%n";

        System.out.printf(format,"STORAGE CLASS NAME", "RECLAIM POLICY", "VOLUME BIDING MODE", "PROVISIONER");
        for(Map<String, Object> map : storageClasses) {
            System.out.printf(format,
                    String.valueOf(map.get("name")),
                    (String) map.get("reclaimPolicy"),
                    (String) map.get("volumeBindingMode"),
                    (String) map.get("provisioner"));
        }

        System.out.printf("\n");

        String sparkThriftServerStorageClass = cnsl.readLine("Select Storage Class for Spark Thrift Server(for instance, nfs) : ");
        if(sparkThriftServerStorageClass == null) {
            throw new RuntimeException("spark thrift server storage class is required!");
        }

        System.out.printf("\n");


        System.out.printf(format,"STORAGE CLASS NAME", "RECLAIM POLICY", "VOLUME BIDING MODE", "PROVISIONER");
        for(Map<String, Object> map : storageClasses) {
            System.out.printf(format,
                    String.valueOf(map.get("name")),
                    (String) map.get("reclaimPolicy"),
                    (String) map.get("volumeBindingMode"),
                    (String) map.get("provisioner"));
        }

        System.out.printf("\n");

        String trinoStorageClass = cnsl.readLine("Select Storage Class for Trino : ");
        if(trinoStorageClass == null) {
            throw new RuntimeException("trino storage class is required!");
        }

        System.out.printf("\n");

        // create.
        return CommandUtils.createQueryEngine(
                configProps,
                projectId,
                clusterId,
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint,
                sparkThriftServerStorageClass,
                sparkThriftServerExecutors,
                sparkThriftServerExecutorMemory,
                sparkThriftServerExecutorCores,
                sparkThriftServerDriverMemory,
                trinoWorkers,
                trinoServerMaxMemory,
                trinoCores,
                trinoTempDataStorage,
                trinoDataStorage,
                trinoStorageClass);
    }
}

package com.cloudcheflabs.dataroaster.cli.command.distributedtracing;

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
        description = "Create Distributed Tracing.")
public class CreateDistributedTracing implements Callable<Integer> {

    @CommandLine.ParentCommand
    private DistributedTracing parent;

    @CommandLine.Option(names = {"--ingress-host"}, description = "Ingress Host Name", required = true)
    private String ingressHost;

    @CommandLine.Option(names = {"--elasticsearch-host-port"}, description = "Elasticsearch Host and Port", required = true)
    private String elasticsearchHostPort;

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

        String storageClass = cnsl.readLine("Select Storage Class : ");
        if(storageClass == null) {
            throw new RuntimeException("storage class is required!");
        }

        System.out.printf("\n");

        // create.
        return CommandUtils.createDistributedTracing(
                configProps,
                projectId,
                clusterId,
                storageClass,
                ingressHost,
                elasticsearchHostPort);
    }
}

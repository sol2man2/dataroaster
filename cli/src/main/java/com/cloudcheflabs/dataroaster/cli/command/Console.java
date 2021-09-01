package com.cloudcheflabs.dataroaster.cli.command;

import com.cloudcheflabs.dataroaster.cli.command.backup.Backup;
import com.cloudcheflabs.dataroaster.cli.command.cicd.CiCd;
import com.cloudcheflabs.dataroaster.cli.command.cluster.Cluster;
import com.cloudcheflabs.dataroaster.cli.command.datacatalog.DataCatalog;
import com.cloudcheflabs.dataroaster.cli.command.distributedtracing.DistributedTracing;
import com.cloudcheflabs.dataroaster.cli.command.kubeconfig.Kubeconfig;
import com.cloudcheflabs.dataroaster.cli.command.login.Login;
import com.cloudcheflabs.dataroaster.cli.command.metricsmonitoring.MetricsMonitoring;
import com.cloudcheflabs.dataroaster.cli.command.podlogmonitoring.PodLogMonitoring;
import com.cloudcheflabs.dataroaster.cli.command.privateregistry.PrivateRegistry;
import com.cloudcheflabs.dataroaster.cli.command.project.Project;
import com.cloudcheflabs.dataroaster.cli.command.queryengine.QueryEngine;
import com.cloudcheflabs.dataroaster.cli.command.streaming.Streaming;
import picocli.CommandLine;


@CommandLine.Command(name = "dataroaster",
        subcommands = {
                Login.class,
                Cluster.class,
                Kubeconfig.class,
                Project.class,
                PodLogMonitoring.class,
                MetricsMonitoring.class,
                DistributedTracing.class,
                PrivateRegistry.class,
                CiCd.class,
                Backup.class,
                DataCatalog.class,
                QueryEngine.class,
                Streaming.class,
                CommandLine.HelpCommand.class
        },
        version = "dataroaster 3.0.0",
        description = "DataRoaster CLI Console.")
public class Console implements Runnable {

    @Override
    public void run() { }
}

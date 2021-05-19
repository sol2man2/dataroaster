job "kibana" {
  namespace = "elasticsearch"
  datacenters = ["dc1"]
  type        = "service"
  update {
    max_parallel     = 1
    health_check     = "checks"
    min_healthy_time = "30s"
    healthy_deadline = "5m"
    auto_revert      = true
    canary           = 0
    stagger          = "30s"
  }
  group "kibana-server" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "http" {
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "es-kibana"
    }
    task "kibana" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume      = "ceph-volume"
        destination = "/srv"
        read_only   = false
      }
      config {
        image = "docker.elastic.co/kibana/kibana:7.12.1"
        force_pull = false
        command = "kibana"
        args = [
          "--elasticsearch.url=http://${NOMAD_JOB_NAME}.service.consul:80",
          "--server.host=0.0.0.0",
          "--server.name=${NOMAD_JOB_NAME}.service.consul",
          "--server.port=${NOMAD_PORT_http}",
          "--path.data=/srv/data",
          "--elasticsearch.preserveHost=false",
          "--xpack.apm.ui.enabled=false",
          "--xpack.graph.enabled=false",
          "--xpack.ml.enabled=false",
        ]
        ports = [
          "http"
        ]
        ulimit {
          memlock = "-1"
          nofile = "65536"
          nproc = "65536"
        }
      }
      resources {
        cpu = 100
        memory = 1024
      }
      service {
        name = "es-kibana-http"
        port = "http"
        check {
          name = "http-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
        check {
          name     = "http-http"
          type     = "http"
          path     = "/"
          interval = "5s"
          timeout  = "4s"
        }
      }
    }
  }
}
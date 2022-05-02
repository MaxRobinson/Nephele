****************************
Deploying with Helm
****************************

Nephele is designed to be deployed to a `Kubernetes Cluster <https://kubernetes.io/>`_ using `Helm <https://helm.sh/>`_ to manage deployment.
Helm simplifies configuration of deployments and the act of deploying Nephele to a K8s cluster

See :ref:`deployment_architecture` for explanation. 


Deployment
-------------
For initial installation

.. code-block:: shell

  $ helm install -f values.yml nephele ./quarkus-chart

To upgrade an installation

.. code-block:: shell

    $ helm upgrade -f values.yaml nephele ./quarkus-chart


Helm Configuration
--------------------
To configure a deployment, values from the default `values.yml` can be overridden.

.. code-block:: yaml

    imagePullSecret: <secrete for image pull secret>
    image:
      ## Name of the image you want to build/deploy
      ## Defaults to the release name
      name: nephele
      ## Tag that you want to build/deploy
      tag: 796188c  # currently refers to the Git commit hash for a release

    ## Application/Deployment-specific values (For configuring Deployment, Service, Route, ConfigMap, etc)
    deploy:
      - deploymentName: <name of deployment>
        replicas: 1  # Should only be 1
        resources:
          limits:
            memory: 5Gi  # min required
          requests:
            memory: 5Gi
        serviceType: ClusterIP   # do not change
        ports:  # do not recommend changing
          - name: http
            port: 8080
            targetPort: 8080
            protocol: TCP
        ## Application properties file for configuring the Quarkus app
        applicationProperties:
          ## Determines if application properties should be created in a configmap and mounted to the Quarkus container
          enabled: True  # Do not change
          ## Location to mount the properties file
          mountPath: /deployments/config/
          ## Application properties file contents
          properties:  # these will change the most between deployments
            AWS_ACCESS_KEY_ID: <cred>
            AWS_SECRET_ACCESS_KEY: <cred>
            QUARKUS_S3_ENDPOINT_OVERRIDE: "http://..." # Target S3 URL
            QUARKUS_REDIS_HOSTS: "redis://<deployed redis server to the cluster>:6379"
            QUARKUS_LOG_LEVEL: INFO
            S3MIRROR_SOURCE: "box"  # S3 or Box
            S3MIRROR_TARGET: "S3"  # S3
            S3MIRROR_MODE_INDEX: "True"  # Service provides indexing or not
            S3MIRROR_MODE_MIRROR: "False"  # Service mirrors to target or not
            S3MIRROR_S3_ENDPOINT_OVERRIDE: "http://..."  # Source S3 URL
            S3MIRROR_S3_ACCESS_KEY_ID: <creds>>
            S3MIRROR_S3_SECRET_ACCESS_KEY: <creds>


Example Helm Configuration
-----------------------------
This example configuration sets up the a Nephele deployment as follows:

* 1 service configured for index
* 1 service configured to mirror from Box to a primary S3
* 1 service configured to mirror from the primary S3 to a secondary S3 instance

.. code-block:: yaml

    imagePullSecret: artifactory
    image:
      ## Name of the image you want to build/deploy
      ## Defaults to the release name
      name: nephele
      ## Tag that you want to build/deploy
      tag: 796188c

    ## Application/Deployment-specific values (For configuring Deployment, Service, Route, ConfigMap, etc)
    deploy:
      - deploymentName: indexer
        replicas: 1
        resources:
          limits:
            memory: 5Gi
          requests:
            memory: 5Gi
        serviceType: ClusterIP
        ports:
          - name: http
            port: 8080
            targetPort: 8080
            protocol: TCP
        ## Application properties file for configuring the Quarkus app
        applicationProperties:
          ## Determines if application properties should be created in a configmap and mounted to the Quarkus container
          enabled: True
          ## Location to mount the properties file
          mountPath: /deployments/config/
          ## Application properties file contents
          properties:
            AWS_ACCESS_KEY_ID: <cred>
            AWS_SECRET_ACCESS_KEY: <cred>
            QUARKUS_S3_ENDPOINT_OVERRIDE: "http://minio-instance1"
            QUARKUS_REDIS_HOSTS: "redis://redis-master:6379"
            QUARKUS_LOG_LEVEL: INFO
            S3MIRROR_SOURCE: "box"
            S3MIRROR_TARGET: "S3"
            S3MIRROR_MODE_INDEX: "True"
            S3MIRROR_MODE_MIRROR: "False"
            S3MIRROR_S3_ENDPOINT_OVERRIDE: "http://minio-instance1"
            S3MIRROR_S3_ACCESS_KEY_ID: <cred>
            S3MIRROR_S3_SECRET_ACCESS_KEY: <cred>

      - deploymentName: box-to-s3-mirror
        replicas: 1
        resources:
          limits:
            memory: 5Gi
          requests:
            memory: 5Gi
        serviceType: ClusterIP
        ports:
          - name: http
            port: 8080
            targetPort: 8080
            protocol: TCP
        ## Application properties file for configuring the Quarkus app
        applicationProperties:
          ## Determines if application properties should be created in a configmap and mounted to the Quarkus container
          enabled: True
          ## Location to mount the properties file
          mountPath: /deployments/config/
          ## Application properties file contents
          properties:
            AWS_ACCESS_KEY_ID: <cred>
            AWS_SECRET_ACCESS_KEY: <cred>
            QUARKUS_S3_ENDPOINT_OVERRIDE: "http://minio-instance1"
            QUARKUS_REDIS_HOSTS: "redis://redis-master:6379"
            QUARKUS_LOG_LEVEL: INFO
            S3MIRROR_SOURCE: "box"
            S3MIRROR_TARGET: "S3"
            S3MIRROR_MODE_INDEX: "False"
            S3MIRROR_MODE_MIRROR: "True"
            S3MIRROR_S3_ENDPOINT_OVERRIDE: "http://minio-instance1"
            S3MIRROR_S3_ACCESS_KEY_ID: <cred>
            S3MIRROR_S3_SECRET_ACCESS_KEY: <cred>

    - deploymentName: S3-to-S3-Mirror
        replicas: 1
        resources:
          limits:
            memory: 5Gi
          requests:
            memory: 5Gi
        serviceType: ClusterIP
        ports:
          - name: http
            port: 8080
            targetPort: 8080
            protocol: TCP
        ## Application properties file for configuring the Quarkus app
        applicationProperties:
          ## Determines if application properties should be created in a configmap and mounted to the Quarkus container
          enabled: True
          ## Location to mount the properties file
          mountPath: /deployments/config/
          ## Application properties file contents
          properties:
            AWS_ACCESS_KEY_ID: <cred>
            AWS_SECRET_ACCESS_KEY: <cred>
            QUARKUS_S3_ENDPOINT_OVERRIDE: "http://minio-instance2"
            QUARKUS_REDIS_HOSTS: "redis://redis-master:6379"
            QUARKUS_LOG_LEVEL: INFO
            S3MIRROR_SOURCE: "S3"
            S3MIRROR_TARGET: "S3"
            S3MIRROR_MODE_INDEX: "False"
            S3MIRROR_MODE_MIRROR: "True"
            S3MIRROR_S3_ENDPOINT_OVERRIDE: "http://minio-instance1"
            S3MIRROR_S3_ACCESS_KEY_ID: <cred>
            S3MIRROR_S3_SECRET_ACCESS_KEY: <cred>







.. _deploying nephele:

Deploying Nephele
====================
Nephele can be deploye several ways.
For production, it is recommended to deploy to a Kubernetes cluster using the helm chart.

To use Nephele in a custom manner:

#. Clone `the repository <https://www.github.com/MaxRobinson/Nephele>`_.
#. Build the project using ``Maven`` and Java 11
#. Create a custom Docker image using Jib

   * :code:`mvn clean package -Dquarkus.container-image.build=true`
#. Tag the image
#. Push the image for use with Helm deployment.



.. toctree::
   :hidden:

   deployment_view
   helm


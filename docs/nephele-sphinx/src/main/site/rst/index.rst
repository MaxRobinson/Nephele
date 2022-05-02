Nephele Manual
======================

Nephele is a distributed, cloud native, asset mirror.
Nephele takes a centrally located set of files, artifacts, or other objects, indexes the objects and uploads them to any number of target locations in near real time.
The objects to index and upload to target locations arespecified via a configuration.
Users can interact with Nephele via a REST API that provides mirroring status, configuration, and object insight.

Functionality
--------------
* Indexing a configured layout of objects, index the object metadata for mirrors to use.
* Comparing target location files against indexed objects
* Mirroring (download from source then upload to target) objects to the target environment based on source file differences, recency, and existence of objects in the target environment.
* Providing a REST API to interact with Nephele.

Nephele currently integrates with Box and AWS S3 API compatible technologies like, Minio.

Use case
----------
Nephele is useful when there is a centralized **Production** set of resources that developers need in order to do development.
Nephele can mirror the production assets (csv, parquet, pptx, excel, etc.) to individual developer Minio instances for each developer.
Developers can then develop against mirrored assets without modifying product, while still getting updates made to production datasets.

Sources
----------

Index Sources
"""""""""""""""""
* Box

Download Sources
"""""""""""""""""
Where Assets can be downloaded from to upload else where.

* Box
* Minio
* AWS S3

Target Upload Environments
"""""""""""""""""""""""""""""
* Minio (specific target instance, see :ref:`Deploying Nephele <deploying nephele>` for details).

  * Currently requires a unique target instance per target replication environment

    * i.e. If you wish to replicate an asset for 3 developers, each developer needs their own Minio instance.


.. toctree::
   :hidden:

   configuration/mirror_config
   deployment/index

.. _configure_nephele:

****************************
Configuring Nephele
****************************

Nephele uses a ``yaml`` file to specify which source files to index and mirror to the target location.
The configuration file is located in the source code at ``src/main/resources/config.yml``.

The configuration file allows Nephele to specify which source **directories** should be indexed and mirrored to target instances.


Config
---------
The configuration file has 1 main portion, the upload list.
The upload list contains a list of objects.
The objects each have 5 parameters

#. ``path`` - String: the source path to the directory to mirror
#. ``recursive`` - boolean: If contents should be uploaded recursively (i.e. upload sub directories)
#. ``last_mod_time_delta_days`` - int: the number of days since a file was last modified. Items modified more recently than this number of days ago, are uploaded indexed and mirrored.
#. ``dated_directories`` - boolean: True if the folder contains subfolders who's names are dates. (**Not currently used, but requires a value**)
#. ``always_upload_root_files`` - boolean: If True, **always** index and mirror files in the specified directory (not recursive) regardless of the time the file was last modified.

Sample:

.. code-block:: yaml

    root_dir: Test Root Directory/main
    dev_instance: false

    upload:
      - path: Test Root Directory/main/Data/Reference Data (census, FIPS, state_rate, etc.)
        recursive: True
        last_mod_time_delta_days: 1095  # 3 years
        dated_directories: False
        always_upload_root_files: True

Sample
-------
The following sample configuration specifies 3 source directories to index and mirror.

The first directory is approximately a perpetual index and upload config for a directory.
Files up to 3 years old will continued to be indexed and mirrored to target instances.
This is useful for artifacts that must be available regardless of age out policies in target instances.
Example: if a target S3 instance has a configuration to age out files after 2 weeks, after 2 weeks artifacts under this directory will be re-uploaded after the original files age out.

Directory 2 is a directory that contains directories with dated folder names and needs files modified up to 14 days ago indexed and mirrored recursively for all nested folders.

Directory 3 is a directory that requires files in the root of the directory to always be uploaded, regardless of how old the files are and does not upload any sub folder files.
This is useful if a directory always needs the content in it indexed and mirrored to targets.

.. code-block:: yaml

    root_dir: Test Root Directory/main
    dev_instance: false

    upload:
      - path: Test Root Directory/main/Data/Reference Data (census, FIPS, state_rate, etc.)
        recursive: True
        last_mod_time_delta_days: 1095  # 3 years
        dated_directories: False
        always_upload_root_files: True

      - path: Test Root Directory/main/Data/Reports(daily, weekly, etc.)
        recursive: True
        last_mod_time_delta_days: 14
        dated_directories: True
        always_upload_root_files: True

      - path: Test Root Directory/main/Report Dirs/Report Deliverables
        recursive: False
        last_mod_time_delta_days: 8
        dated_directories: False
        always_upload_root_files: True


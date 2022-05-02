# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
import os
import sys


# -- Project information -----------------------------------------------------


project = '${app.name}'
copyright = '2021, Johns Hopkins University Applied Physics Laboratory'
author = 'Johns Hopkins University Applied Physics Laboratory'
version = '${version}'
release = '${version}'


# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx_copybutton'
]

# The file extensions of source files. Sphinx considers the files with this suffix as sources.
# The value can be a dictionary mapping file extensions to file types.
source_suffix = {
    '.rst': 'restructuredtext'
}

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = []

# The document name of the "root" document, that is, the document that contains the root toctree directive. Default is 'index'.
root_doc = 'index'

# Minimum sphinx version
needs_sphinx = '4.0'


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'furo'
html_logo = "_static/Nephele-Logo.png"

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']

# Output file base name for HTML help builder. Default is 'pydoc'.
htmlhelp_basename = '${pdf.name}doc'

# -- Options for LaTeX output ---------------------------------------------

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title,
#  author, documentclass [howto, manual, or own class]).
latex_documents = [
    (root_doc, '${pdf.name}.tex', '${pdf.name}',
     '${organization.name}', 'manual'),
]

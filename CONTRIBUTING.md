# Contributing to JTS

The project welcomes contributions.  JTS is a mature, widely-used library, so contributions will be reviewed carefully to ensure they do not compromise the quality of the codebase.

To start, it helps to announce what you are working on ahead of time to confirm that it is an acceptable contribution.  To do this post to the [JTS Mailing List](https://locationtech.org/mailman/listinfo/jts-dev). 

The following sections outline workflows involved with contributions.

## Become a Contributor

In order to have code contributions accepted, the submitter must:

1.  Sign the [Eclipse Contributor Agreement](https://www.eclipse.org/legal/ECA.php) (ECA) with the Eclipse Foundation.  Use the registered email with the Git commits associated to the GitHub pull request.

## Develop a Contribution

Code contributions must include a license header at the top of each file.  A sample header for Java files is [here](doc/sample_java_header.txt). 

Code contributions should include the following:

* Javadoc on classes and methods
* Unit Tests to demonstrate code correctness and allow this to be maintained going forward.  In the case of bug fixes the unit test should demonstrate the bug in the absence of the fix (if any).  Unit Tests are usually JUnit classes, or in some cases may be JTS TestRunner test cases
* License information on __all__ provided source files. An example license header can be found [here](doc/sample_java_header.txt). 

## Make a Pull Request

The easiest and most visible way to make a contribution is to submit a Pull Request (PR) to the [GitHub repo](https://github.com/locationtech/jts).  

When preparing a PR please do the following:

1.  Acknowledge that the code contribution is IP clean by 'signing off' the commits in the pull request with the '-s' option to 'git commit'.

2. Labeling the PR helps to track and prioritize the contribution.  Use the following labels:
   * The contribution type: enhancement, bug, api
   * The module being contributed to: jts-core, jst-io, jts-app, jts-doc, jts-build

3. When a PR is submitted Travis CI will run to check the commiter Eclipse status and the build correctness.  
  Please check the PR status, and fix any reported problems

If you have questions about this, please ask on the jts-dev list.

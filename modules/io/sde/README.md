The jt-sde module requires the ESRI ArcSDE jars included with your software be installed in your local maven repository:

    mvn install:install-file -Dfile=jsde91_sdk.jar \
       -DgroupId=com.esri -DartifactId=jsde_sdk \
       -Dversion=9.1 -Dpackaging=jar -DgeneratePom=true

    mvn install:install-file -Dfile=jpe91_sdk.jar \
       -DgroupId=com.esri -DartifactId=jpe_sdk \
       -Dversion=9.1 -Dpackaging=jar -DgeneratePom=true
       
You can then build using the arcsde profile:

    mvn -Parcsde
    
The default SDE version is 9.1. if you want to use newer ESRI ArcSDE Java API jars, provided that you installed them through mvn install:install-file in your local repo, run maven with:

    mvn -Parcsde -Dsde.version=9.2
  

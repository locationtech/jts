# JTS TestBuilder

The TestBuilder is a GUI application which allows creating, editing and visualizing geometries, and executing JTS functions on them.

* Run (from project root), with larger memory: 
     
       java -jar modules/app/target/JTSTestBuilder.jar -Xmx2000M 
     
* Run (with Metal L&F - useful on MacOS)

       java -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel -jar modules/app/target/JTSTestBuilder.jar -Xmx2000M
       
![](JTSTestuilder.png)

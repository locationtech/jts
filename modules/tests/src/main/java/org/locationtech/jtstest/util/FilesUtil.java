package org.locationtech.jtstest.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FilesUtil {

  public static List expand(Collection fileAndDirectoryNames) {
    List filenames = new ArrayList();
    for (Iterator i = fileAndDirectoryNames.iterator(); i.hasNext();) {
      String name = (String) i.next();
      File file = new File(name);
      if (file.isDirectory()) {
        filenames.addAll(expand(file));
      } else if (file.isFile()) {
        filenames.add(name);
      }
    }
    return filenames;
  }

  public static List expand(Collection fileAndDirectoryNames, String fileExtension) {
    List filenames = new ArrayList();
    for (Iterator i = fileAndDirectoryNames.iterator(); i.hasNext();) {
      String name = (String) i.next();
      File file = new File(name);
      if (file.isDirectory()) {
        filenames.addAll(expand(file, fileExtension));
      } else if (file.isFile()) {
        filenames.add(name);
      }
    }
    return filenames;
  }

  public static List expand(File fileOrDir) {
    List filenames = new ArrayList();
    if (fileOrDir.isDirectory()) {
      File[] files = fileOrDir.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isFile()) {
          filenames.add(files[i].getPath());
        }
      }
    }
    else {
      filenames.add(fileOrDir.getPath());
    }
    return filenames;
  }
  
  public static List expand(File fileOrDir, String fileExtension) {
    List filenames = new ArrayList();
    if (fileOrDir.isDirectory()) {
      File[] files = fileOrDir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("." + fileExtension);
        }
      });
      for (int i = 0; i < files.length; i++) {
        if (files[i].isFile()) {
          filenames.add(files[i].getPath());
        }
      }
    } else {
      filenames.add(fileOrDir.getPath());
    }
    return filenames;
  }

	public static Collection filenamesDeep(File directory) {
		Collection filenames = new ArrayList();
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				filenames.add(files[i].getPath());
			} else if (files[i].isDirectory()) {
				filenames.add(filenamesDeep(files[i]));
			}
		}
		return filenames;
	}

	public static List<File> toFile(List<String> filenames) {
		List<File> files = new ArrayList<File>();
		for (String filename : filenames) {
			files.add(new File(filename));
		}
		return files;
	}

}

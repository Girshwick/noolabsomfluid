package org.NooLab.somtransform.algo.clazz;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



/**
 * 
 * see also: http://www.philipphauer.de/study/se/classloader.php
 *           http://stackoverflow.com/questions/2215843/using-reflection-in-java-to-create-a-new-instance-with-the-reference-variable-ty
 * 
 * @author kwa
 *
 */
public class Packages {

	public Packages() {

	}


	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to support use of JAR files
	 *
	 * @param packageName The base package
	 * @param regexFilter an optional class name pattern.
	 * @return The classes
	 */
	public Class[] getClassesInPackage(String packageName, String regexFilter) {
		
		ClassLoader classLoader;
		ArrayList<Class> classList ;
		TreeSet<String> classes ;
		String path;
		Enumeration<URL> resources;
		ArrayList<String> dirs;
		
		
		Pattern regex = null;
		if (regexFilter != null)
			regex = Pattern.compile(regexFilter);

		try {
			classLoader = Thread.currentThread().getContextClassLoader();
			assert classLoader != null;
			
			path = packageName.replace('.', '/');
			resources = classLoader.getResources(path);
			dirs = new ArrayList<String>();
			
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(resource.getFile());
			}
			
			classes = new TreeSet<String>();
			for (String directory : dirs) {
				classes.addAll(findClasses(directory, packageName, regex));
			}

			classList = new ArrayList<Class>();
			
			for (String clazz : classes) {
				classList.add(Class.forName(clazz));
			}
			
			return classList.toArray(new Class[classes.size()]);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Recursive method used to find all classes in a given path (directory or zip file url).  Directories
	 * are searched recursively.  (zip files are
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to support use of JAR files
	 *
	 * @param path   The base directory or url from which to search.
	 * @param packageName The package name for classes found inside the base directory
	 * @param regex       an optional class name pattern.  e.g. .*Test
	 * @return The classes
	 */
	private static TreeSet<String> findClasses(String path, String packageName, Pattern regex) throws Exception {
		TreeSet<String> classes = new TreeSet<String>();
		if (path.startsWith("file:") && path.contains("!")) {
			String[] split = path.split("!");
			URL jar = new URL(split[0]);
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					String className = entry.getName().replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
					if (className.startsWith(packageName) && (regex == null || regex.matcher(className).matches()))
						classes.add(className);
				}
			}
		}
		File dir = new File(path);
		if (!dir.exists()) {
			return classes;
		}
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file.getAbsolutePath(), packageName + "." + file.getName(), regex));
			} else if (file.getName().endsWith(".class")) {
				String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
				if (regex == null || regex.matcher(className).matches())
					classes.add(className);
			}
		}
		return classes;
	}
 
  
	
	
}

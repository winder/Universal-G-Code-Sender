/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>simon.mieth@gmx.de</a>
 *
 */
public class Loader {
	
	
	
    public static final String OPTION_MAIN_CLASS="-main";
    public static final String OPTION_LIB_FOLDER="-lib";
    public static final String OPTION_CLASSES_FOLDER="-classes";
    public String mainClass = "org.kabeja.Main";

    private Set classpathEntries = new HashSet();
    
    
    
    
    public Loader() {
		this.classpathEntries.add("lib");
		this.classpathEntries.add("classes");

	}

	public static void main(String[] args) {
        Loader l = new Loader();
        l.launch(args);
    }

    public void launch(String[] args) {
        args = parseMainClass(args);

        URLClassLoader cl = new URLClassLoader(getClasspath());

        try {
            Class clazz = cl.loadClass(this.mainClass);
            Object obj = clazz.newInstance();

            // init the project
            Method method = clazz.getDeclaredMethod("main",
                    new Class[] { args.getClass() });
            method.invoke(obj, new Object[] { args });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        }
    }

    protected URL[] getClasspath() {
        List urls = new ArrayList();

         Iterator i = this.classpathEntries.iterator();
         while(i.hasNext()){
            File f = new File((String)i.next());

            try {
                if (f.isDirectory() && f.exists()) {
                    File[] files = f.listFiles();

                    for (int x = 0; x < files.length; x++) {
                        String name = files[x].getName().toLowerCase();

                        if (name.endsWith(".jar") || name.endsWith(".zip")) {
                            urls.add(files[x].toURL());
                        }
                    }
                }

                urls.add(f.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    protected String[] parseMainClass(String[] args) {
        List list = new ArrayList();

        for (int i = 0; i < args.length; i++) {
            if (OPTION_MAIN_CLASS.equals(args[i]) && ((i + 1) < args.length)) {
                i++;
                this.mainClass = args[i];
            }else if(OPTION_LIB_FOLDER.equals(args[i])){
            	i++;
            	this.addPathEntries(args[i]);
            }else if(OPTION_CLASSES_FOLDER.equals(args[i])){
            	i++;
            	this.addPathEntries(args[i]);
            }else {
                list.add(args[i]);
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }
    
    
    protected void addPathEntries(String path){
    	StringTokenizer st = new StringTokenizer(path,":");
    
    	while(st.hasMoreElements()){
    		String el = (String)st.nextElement();
    		this.classpathEntries.add(el);
    	}
    }
    
}

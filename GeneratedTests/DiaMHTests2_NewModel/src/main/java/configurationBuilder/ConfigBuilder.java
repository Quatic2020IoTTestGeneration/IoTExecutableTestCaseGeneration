package configurationBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import annots.ModelName;
import annots.Omitted;
import org.json.simple.JSONObject;


public class ConfigBuilder {
	
	public static JSONObject extract(String loaderPath, String clazzPath, String pkgName) throws ClassNotFoundException, MalformedURLException {
		File dir = new File(clazzPath);
		File loadDir = new File(loaderPath);
		URL url = loadDir.toURI().toURL();
		URL[] urls = new URL[] {url};
		ClassLoader cl = new URLClassLoader(urls);
		JSONObject classes = new JSONObject();
		for(File file : dir.listFiles()) {
			String clazzName = file.getName().replace(".class", "");
			Class<?> clazz = Class.forName(pkgName+"."+clazzName);
			Annotation[] ants = clazz.getDeclaredAnnotationsByType(ModelName.class);
			if(ants.length > 0) {
				System.out.println("Processing class "+clazz.getName());
				ModelName clazzAnnot = (ModelName) ants[0];
				String clazzModNam = clazzAnnot.value();
				System.out.println("Model name: "+clazzModNam);
				
				JSONObject methods = new JSONObject();
				for(Method m : clazz.getDeclaredMethods()) {
					Annotation[] ma = m.getDeclaredAnnotationsByType(ModelName.class);
					
					if(ma.length > 0) {
						System.out.println("Method: "+clazzName+"."+m.getName());
						String mFullName = m.getName();
						ModelName methodAnnot = (ModelName) ma[0];
						String methModNam = methodAnnot.value();
						String methActualNam = mFullName.substring(mFullName.indexOf(".")+1);
						System.out.println("Model name: "+methModNam);
						Parameter[] params = m.getParameters();
						List<String> testParams = new LinkedList<>();
						for(Parameter p : params) {
							Annotation[] pa = p.getDeclaredAnnotationsByType(Omitted.class);
							if(pa.length > 0) {
								System.out.println("Omitted parameter: "+p.getName());
								testParams.add(p.getName());
							}
						}
						JSONObject currMethod = new JSONObject();
						currMethod.put("function", methActualNam);
						if(testParams.isEmpty()) {
							currMethod.put("params", "");
						}
						else {
							currMethod.put("params", testParams.toString().substring(1, testParams.toString().length()-1));
						}
						
						methods.put(methModNam, currMethod);
					}
				}
				classes.put(clazzModNam, methods);
			}
			
		}
		return classes;
	}
	
	/**
	 * 
	 * @param args:
	 * 		0: class path for this project (target/classes)
	 * 		1: path of built wrapper classes (target/classes/DiaMHTestsMaven/wrappers)
	 * 		2: package name for the wrapper classes (DiaMHTestsMaven.wrappers)
	 * 		3: path for wrappers configuration file (../../TestGenerationTool/config/wrappers.json)
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws ClassNotFoundException, MalformedURLException {
		JSONObject res = extract(args[0], args[1], args[2]);
        try (FileWriter file = new FileWriter(args[3])) {
        	 
            file.write(res.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

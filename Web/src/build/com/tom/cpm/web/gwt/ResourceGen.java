package com.tom.cpm.web.gwt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ResourceGen {
	private static Path ent;
	private static boolean min;
	private static PrintWriter wr;

	public static String run(File wd, boolean min) {
		//File out = new File(wd, min ? "resources.min.js" : "resources.js");
		ResourceGen.min = min;
		StringWriter w = new StringWriter();
		try (PrintWriter wr = new PrintWriter(w)){
			ResourceGen.wr = wr;
			run(new File(wd, "../CustomPlayerModels/src/shared/resources"));
			run(new File(wd, "../CustomPlayerModels-EditorWeb/src/main/resources"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Generated Resources");
		return w.toString();
	}

	private static void run(File entry) throws IOException {
		ent = entry.toPath();
		Files.walk(ent).forEach(ResourceGen::process);
	}

	private static void process(Path p) {
		Path r = ent.relativize(p);
		File f = p.toFile();
		if(f.exists() && !f.isDirectory() && !f.getName().endsWith(".xcf")) {
			String path = r.toString().replace('\\', '/');
			if(min && path.startsWith("assets/cpm/textures/") && !path.endsWith("/cape.png") && !path.contains("/armor") && !path.endsWith("/elytra.png") && !path.endsWith("/slim.png") && !path.endsWith("/default.png") && !path.endsWith("free_space_template.png"))return;
			if(min && path.startsWith("assets/cpm/wiki/"))return;
			if(path.equals("icon.png") || path.endsWith(".lang"))return;
			wr.print("i(\"");
			wr.print(path);
			wr.print("\", \"");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try(InputStream is = new FileInputStream(f)) {
				byte[] data = new byte[1024];
				int n;
				while((n = is.read(data)) > 0) {
					baos.write(data, 0, n);
				}
			} catch (IOException e) {
			}
			wr.print(Base64.getEncoder().encodeToString(baos.toByteArray()));
			wr.println("\");");
		}
	}
}
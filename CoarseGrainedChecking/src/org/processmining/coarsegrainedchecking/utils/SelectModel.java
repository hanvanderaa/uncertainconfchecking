package org.processmining.coarsegrainedchecking.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

public class SelectModel {

	public static void main(String[] args) throws Exception {

		HashSet<String> relevantModels = new HashSet<String>();
		try (BufferedReader br = new BufferedReader(new FileReader("input/RelevantModels.txt"))) {
			String line = br.readLine();
			while (line != null) {
				relevantModels.add(line);
				line = br.readLine();
			}
		}

		File dir = new File("input/models3");
		int f = 0;
		for (File file : dir.listFiles()) {
			System.out.println(file);
			boolean relevant = false;
			for (String r : relevantModels) {
				if (file.getName().contains(r)) {
					relevant = true;
					continue;
				}
			}
			if (relevant) {
				f++;
			} else {
				file.delete();
			}
		}
		System.out.println(f);

	}

}

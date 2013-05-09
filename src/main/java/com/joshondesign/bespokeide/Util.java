/*
 * Copyright 2013 Adam L. Davis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshondesign.bespokeide;

import java.io.File;
import java.util.Arrays;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * @author Adam L. Davis
 * 
 */
public class Util {

	private Util() {
	}

	public static int hashcode(Object... args) {
		return Arrays.hashCode(args);
	}

	public static File getUserHome() {
		String home = System.getProperty("user.home");
		System.out.println("OS current temporary directory is " + home);
		if (home == null) {
			return new File("/");
		}
		return new File(home);
	}

	public static File getTempDir() {
		String temp = System.getProperty("java.io.tmpdir");
		System.out.println("OS current temporary directory is " + temp);
		if (null==temp) {
			return new File("/tmp");
		}
		return new File(temp);
	}
	
	// uses Guava's splitter to split, then uses second
	// line of code to find good whitespace splitter.
	public static TextArray codeToTextArray(CharSequence methodCode) {
		final Splitter splitter = Splitter.on('\n');
		final Iterable<String> lines = splitter.split(methodCode);
		String sep = "";
		
		for (String s : lines) {
			final String s2 = CharMatcher.WHITESPACE.trimLeadingFrom(s);
			if (s2.length() < s.length()) {
				sep = s.substring(0, (s.length() - s2.length()));
				break;
			}
		}
		if ("".equals(sep)) {
			return new TextArray(new CharSequence[] {methodCode}, "");
		}
		return new TextArray(Splitter.on(sep).split(methodCode), sep);
	}

}

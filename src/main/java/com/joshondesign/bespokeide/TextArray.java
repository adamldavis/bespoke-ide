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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Wraps an array of CharSequences with a separator CharSequence.
 * 
 * @author Adam L. Davis
 */
public class TextArray implements CharSequence, Iterable<CharSequence> {

	private final CharSequence[] array;
	private final CharSequence separator;

	public TextArray(CharSequence[] array, CharSequence separator) {
		super();
		this.array = array;
		this.separator = separator;
	}

	public TextArray(Iterable<String> strings, String separator) {
		super();
		final List<String> list = new LinkedList<>();
		for (String s : strings) {
			list.add(s);
		}
		this.array = list.toArray(new CharSequence[list.size()]);
		this.separator = separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		return toString().charAt(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		if (array.length == 0) {
			return 0;
		}
		int total = separator.length() * (array.length - 1);
		for (CharSequence s : array)
			total += s.length();
		return total;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int begin, int end) {
		return toString().subSequence(begin, end);
	}

	@Override
	public Iterator<CharSequence> iterator() {
		return Arrays.asList(array).iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CharSequence s : array) {
			if (sb.length() == 0) {
				sb.append(s);
			} else {
				sb.append(separator).append(s);
			}
		}
		return sb.toString();
	}

}

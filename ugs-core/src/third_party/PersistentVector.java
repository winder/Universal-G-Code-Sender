/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: welker $'
 * '$Date: 2010-05-05 22:21:26 -0700 (Wed, 05 May 2010) $' 
 * '$Revision: 24234 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

/**
 * Class that implements a random access file for storing multiple objects that would take up too
 * much room in memory. Used here for storing a collection of Strings (records) for very large
 * text based data sets. When combined with the PersistentVector class and PersistentTableModel class
 * this gives the ability to have tables of almost unlimited size (since only a vector pointing to
 * locations in the object file is stored in RAM)
 */

package third_party;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

/* 
 A vector-like class that uses persistent storage (ObjectFile) rather than RAM for
 storage. 
 Note that some sorting tests indicate that access of disk-based objects is 
 roughly 100 times slower than a RAM based implementation.
 Thus, to speed up some operations, a RAM based cache of the first N items 
 in the collection will be implemented
 */

public class PersistentVector {
	private static int objNum = 0;

	/**
	 * allow for header rows
	 */
	public int firstRow = 0;

	/**
	 * number of items to be stored in memory This parameter should be as large
	 * as possible when a PersistentVector is used in the PersistentTableModel
	 * class because sorting of tables from disk is much slower than sorting
	 * from memory (presumably because of the seek time needed to access values
	 * for comparisons) For other uses, it can be set to 0 which puts all values
	 * on disk.
	 */
	private int inMemoryNum = 200;

	/*
	 * a vector that contains either the object or a long pointer to the
	 * position of the object in a random access file
	 */
	private Vector objectList;

	/*
	 * the ObjectFile where objects are actually stored
	 */
	private ObjectFile obj;

	/*
	 * the name of the ObjectFile
	 */
	private String objName = "ObjectFile";

	/*
	 * field delimiter string
	 */
	private String field_delimiter = "#x09";

	/*
	 * header line vector
	 */
	private Vector headerLinesVector;

	/*
	 * tmp directory for object file
	 */
	private String tmpDir = "./";

	/**
	 * flag to indicate whether to ignore consequtiveDelimiters
	 */
	private boolean ignoreConsecutiveDelimiters = true;

	/**
	 * Constructor of PersistentVector
	 * 
	 */
	public PersistentVector() {
		// call the other constructor with a null override
		this(null);
	}

	/**
	 * Constructor of PersistentVector
	 * 
	 */
	public PersistentVector(String tempDirOverride) {
		objectList = new Vector(inMemoryNum + 1);
		objNum++;
		getTempDirFromConfig();
		if (tempDirOverride != null) {
			this.tmpDir = tempDirOverride;
		}
		try {
			if (tmpDir != null) {
				objName = tmpDir + objName + objNum;
			} else {
				objName = objName + objNum;
			}
			obj = new ObjectFile(objName);
		} catch (Exception w) {
		}
	}

	public void setIgnoreConsecutiveDelimiters(boolean flag) {
		ignoreConsecutiveDelimiters = flag;
	}

	/*
	 * Method to get tmp directory for object file
	 */
	private void getTempDirFromConfig() {

	}

	/*
	 * needed for skipping over comments, header info
	 */
	public void setFirstRow(int frow) {
		this.firstRow = frow;
	}

	/**
	 * Method to get the value of first Row
	 */
	public int getFirstRow() {
		return firstRow;
	}

	/**
	 * Method to get headerLinesVector
	 * 
	 */
	public Vector getHeaderLinesVector() {
		return headerLinesVector;
	}

	/**
	 * Method to set a vector as a header lines vector
	 * 
	 * @param newHeaderLinesVector
	 */
	public void setHeaderLinesVector(Vector newHeaderLinesVector) {
		headerLinesVector = newHeaderLinesVector;
	}

	/**
	 * Method set tmp directory for Object file
	 * 
	 * @param directory
	 *            the tmp directory will be set
	 */
	public void setTmpDir(String directory) {
		tmpDir = directory;
	}

	/*
	 * read a text file and store each line as an object in an ObjectFile
	 */
	public void init(String filename) {
		String temp;
		int nlines;
		File f = new File(filename);
		try {
			BufferedReader in = new BufferedReader(new java.io.FileReader(f));
			nlines = 0;
			long pos = 0;
			headerLinesVector = new Vector();
			try {
				while (((temp = in.readLine()) != null)) {
					if (temp.length() > 0) { // do not count blank lines
						nlines++;
						if (nlines > firstRow) {
							String[] tempA = getColumnValues(temp);
							if (nlines > inMemoryNum) {
								pos = obj.writeObject(tempA); // object added to
								// file
								Long lpos = new Long(pos);
								objectList.addElement(lpos); // position added
								// to objectList
							} else {
								objectList.addElement(tempA);
							}
						} else { // header info
							headerLinesVector.addElement(temp);
						}
					}
				}
				in.close();

			} catch (Exception e) {
			};
		} catch (Exception w) {
		};
	}

	/*
	 * read a text file and store each line as an object in an ObjectFile each
	 * line in the file (assumed to be a text file) is turned into an array of
	 * strings
	 * 
	 * start with a file object
	 */
	public void init(File f) {
		String temp;
		int nlines;
		try {
			BufferedReader in = new BufferedReader(new java.io.FileReader(f));
			nlines = 0;
			long pos = 0;
			headerLinesVector = new Vector();
			try {
				while (((temp = in.readLine()) != null)) {
					if (temp.length() > 0) { // do not count blank lines
						nlines++;
						if (nlines > firstRow) {
							String[] tempA = getColumnValues(temp);
							if (nlines > inMemoryNum) {
								pos = obj.writeObject(tempA); // object added to
								// file
								Long lpos = new Long(pos);
								objectList.addElement(lpos); // position added
								// to objectList
							} else {
								objectList.addElement(tempA);
							}
						} else { // header info
							headerLinesVector.addElement(temp);
						}
					}
				}
				in.close();
			} catch (Exception e) {
			};
		} catch (Exception w) {
		};
	}

	public void init(File f, int fRow) {
		this.firstRow = fRow;
		init(f);
	}

	/*
	 * initiallize with a single element
	 */
	public void initEmpty(String[] ar, int numEmptyRows) {
		try {
			for (int i = 0; i < numEmptyRows; i++) {
				long pos = obj.writeObject(ar); // object added to file
				Long lpos = new Long(pos);
				objectList.addElement(lpos); // position added to objectList
			}
		} catch (Exception e) {
		}
	}

	public void setFieldDelimiter(String s) {
		this.field_delimiter = s.trim();
	}

	/**
	 * Method to get field delimiter
	 */
	public String getFieldDelimiter() {
		return field_delimiter;
	}

	/*
	 * write a text file from the pv
	 */
	public void writeObjects(String filename) {
		File f = new File(filename);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			// write header lines if they exist
			if (headerLinesVector != null) {
				for (int jj = 0; jj < headerLinesVector.size(); jj++) {
					String hline = (String) headerLinesVector.elementAt(jj);
					out.write(hline, 0, hline.length());
					out.newLine();
				}
			}
			for (int i = 0; i < this.size(); i++) {
				String[] s = (String[]) this.elementAt(i);
				String sss = s[0];
				for (int ii = 1; ii < s.length; ii++) {
					// sss = sss + "\t" + s[ii];
					sss = sss + getDelimiterString() + s[ii];
				}
				out.write(sss, 0, sss.length());
				out.newLine();
			}
			out.flush();
			out.close();
		} catch (Exception e) {
		}
	}

	public Object elementAt(int iii) {
		Object o = objectList.elementAt(iii);
		try {
			if (Long.class.isInstance(o)) {
				Long lll = (Long) objectList.elementAt(iii);
				o = obj.readObject(lll.longValue());
			}
		} catch (Exception e) {
		}
		return o;
	}

	public void addElement(Serializable o) {
		try {
			if (objectList.size() > inMemoryNum) {
				long pos = obj.writeObject(o); // object added to end of file
				Long lpos = new Long(pos);
				objectList.addElement(lpos);
			} else {

				objectList.addElement(o);
			}
		} catch (Exception w) {
		}
	}

	public void setElementAt(Serializable o, int i) {
		try {
			if (i > inMemoryNum) {
				long pos = obj.writeObject(o); // object added to end of file
				Long lpos = new Long(pos);
				objectList.setElementAt(lpos, i);
			} else {
				objectList.setElementAt(o, i);
			}
		} catch (Exception w) {
		}
	}

	public void insertElementAt(Serializable o, int i) {
		try {
			// if (i>inMemoryNum) {
			long pos = obj.writeObject(o); // object added to end of file
			Long lpos = new Long(pos);
			objectList.insertElementAt(lpos, i);
			// }
			// else {
			//          
			// }
		} catch (Exception w) {
		}
	}

	public void removeElementAt(int i) {
		objectList.removeElementAt(i);
	}

	public int size() {
		if (objectList == null)
			return 0;
		else
			return objectList.size();
	}

	public void removeAllElements() {
		objectList.removeAllElements();
		obj.delete();
		try {
			obj = new ObjectFile(objName + objNum);
		} catch (Exception w) {
		}
	}

	public void delete() {
		obj.delete();
		objectList = null;
	}

	/**
	 * Method to assign a vector to objectList
	 * 
	 * @param newObjectList
	 *            the new object list vector
	 */
	public void setObjectList(Vector newObjectList) {
		objectList = newObjectList;
	}

	private String[] getColumnValues(String str) {
		// add a space in case line text ends in a delimiter
		// to insure that an empty string is saved
		str = str + " ";
		String sDelim = getDelimiterString();
		String oldToken = "";
		String token = "";
		Vector res = new Vector();
		// boolean ignoreConsequtiveDelimiters = false;
		if (ignoreConsecutiveDelimiters) {
			StringTokenizer st = new StringTokenizer(str, sDelim, false);
			while (st.hasMoreTokens()) {
				token = st.nextToken().trim();
				res.addElement(token);
			}
		} else {
			StringTokenizer st = new StringTokenizer(str, sDelim, true);
			while (st.hasMoreTokens()) {
				token = st.nextToken().trim();
				if (!inDelimiterList(token, sDelim)) {
					res.addElement(token);
				} else {
					if ((inDelimiterList(oldToken, sDelim))
							&& (inDelimiterList(token, sDelim))) {
						res.addElement("");
					}
				}
				oldToken = token;
			}
		}
		String[] vals = new String[res.size()];
		for (int i = 0; i < res.size(); i++) {
			vals[i] = (String) res.elementAt(i);
		}
		return vals;
	}

	private boolean inDelimiterList(String token, String delim) {
		boolean result = false;
		int test = delim.indexOf(token);
		if (test > -1) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/*
	 * this method converts hex delimiter strings to in-line strings i.e. #x09
	 * is converted to a tab \t
	 */
	private String getDelimiterString() {
		String str = "";
		String temp = field_delimiter;
		if (temp.startsWith("#x")) {
			temp = temp.substring(2);
			if (temp.equals("0A"))
				str = "\n";
			if (temp.equals("09"))
				str = "\t";
			if (temp.equals("20"))
				str = " ";
		} else {
			str = temp;
		}
		return str;
	}

	/*
	 * setter for the number of object stored in RAM before going to disk
	 */
	public void setInMemoryNum(int num) {
		this.inMemoryNum = num;
	}

	/*
	 * getter for the number of object stored in RAM before going to disk
	 */
	public int getInMemoryNum() {
		return inMemoryNum;
	}

	static public void main(String args[]) {
		PersistentVector pv = new PersistentVector();
		System.out.println("New Persistent Vector created.");
		pv.init("C:/VisualCafe/Projects/PersistentVector/test.txt");
		System.out.println("Object File has been filled!");
		String s = (String) pv.elementAt(0);
		System.out.println(s);
		String s1 = (String) pv.elementAt(100);
		System.out.println(s1);
		pv.insertElementAt("DFH DFH DFH", 3);
		pv.writeObjects("C:/VisualCafe/Projects/PersistentVector/test1.txt");
	}

}
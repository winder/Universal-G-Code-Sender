/*
 * Copyright (c) 2003-2010 The Regents of the University of California.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class ObjectFile {
	/*
	 * the randomAccessFile used for storage
	 */
	RandomAccessFile dataFile;
	/*
	 * the file name of the random access file
	 */
	String sFileName;

	/*
	 * ObjectFile constructor note the the file is named 'ObjectFilen'
	 */
	public ObjectFile(String sName) throws IOException {
		sFileName = sName;
		File f = new File(sName);
		if (f.exists())
			f.delete();
		dataFile = new RandomAccessFile(sName, "rw");
	}

	/*
	 * write an object to the ObjectFile returns file postion object was written
	 * to.
	 */
	public synchronized long writeObject(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.flush();

		int datalen = baos.size();

		// append record
		long pos = dataFile.length();
		dataFile.seek(pos);

		// write the length of the output
		dataFile.writeInt(datalen);
		dataFile.write(baos.toByteArray());

		baos = null;
		oos = null;

		return pos;
	}

	/*
	 * get the current object length
	 */
	public synchronized int getObjectLength(long lPos) throws IOException {
		dataFile.seek(lPos);
		return dataFile.readInt();
	}

	/*
	 * get an object from the object file
	 */
	public synchronized Object readObject(long lPos) throws IOException,
			ClassNotFoundException {
		dataFile.seek(lPos);
		int datalen = dataFile.readInt();
		if (datalen > dataFile.length()) {
			throw new IOException("Data file is corrupted. datalen: " + datalen);
		}
		byte[] data = new byte[datalen];
		dataFile.readFully(data);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object o = ois.readObject();

		bais = null;
		ois = null;
		data = null;

		return o;
	}

	/*
	 * get the current object File length
	 */
	public long length() throws IOException {
		return dataFile.length();
	}

	/*
	 * close the objectFile
	 */
	public void close() throws IOException {
		dataFile.close();
	}

	/*
	 * delete the ObjectFile
	 */
	public void delete() {
		try {
			dataFile.close();
			File f = new File(sFileName);
			if (f.exists())
				f.delete();
		} catch (Exception w) {
		}
	}

	static public void main(String args[]) {
		String testString = "This is a test!!!";
		try {
			ObjectFile of = new ObjectFile("ObjectFile");
			long pos = of.writeObject(testString);
			Object res = of.readObject(pos);
			System.out.println("Result: " + (String) res);
		} catch (Exception e) {
		}
	}

}